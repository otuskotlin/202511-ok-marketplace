@file:Suppress("UNUSED_PARAMETER") // Подавляем предупреждения о неиспользуемых параметрах в этом файле

package ru.otus.otuskotlin.coroutines

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.runBlocking
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test

/**
 * Класс для демонстрации основ работы с корутинами в Kotlin.
 * Корутины — это легковесные потоки, которые позволяют писать асинхронный код
 * в последовательном стиле, избегая проблем с обратными вызовами (callbacks).
 */
class Ex3Coroutines {

    /**
     * Пример приостанавливаемой (suspend) функции.
     * Модификатор 'suspend' означает, что эта функция может быть приостановлена
     * в середине своего выполнения (в точке вызова другой suspend-функции, например, delay)
     * и возобновлена позже. Такие функции могут вызываться только из других suspend-функций
     * или из корутины.
     */
    private suspend fun someMethod(): String {
        println("Some method: Начало выполнения в корутине")
        // 'delay' — это специальная suspend-функция. Она приостанавливает текущую корутину
        // на указанное время, НЕ блокируя при этом поток, в котором она выполняется.
        // Это позволяет потоку выполнять другую работу в это время.
        delay(1000)
        return "Some data"
    }

    /**
     * Еще одна suspend-функция, принимающая параметры.
     * @param a число для обработки.
     * @param throwPlace параметр для имитации ошибок в разных местах.
     */
    private suspend fun otherMethod(a: Int, throwPlace: Int = 0): Int {
        // Это синхронное исключение. Оно будет выброшено немедленно при вызове функции.
        if (throwPlace == 1) throw RuntimeException("Ошибка 1: Синхронная в otherMethod")
        println("Other method: Начало выполнения с числом $a")
        // Это асинхронное исключение, оно будет выброшено после приостановки.
        if (throwPlace == 2) throw RuntimeException("Ошибка 2: Асинхронная в otherMethod")
        delay(1000)
        return a * 2
    }

    // Неиспользуемая функция, чтобы показать, что suspend-функции нельзя вызывать из обычного кода.
    @Suppress("unused")
    fun x() {
        // Раскомментирование следующей строки приведет к ошибке компиляции:
        // "Suspend function 'someMethod' should be called only from a coroutine or another suspend function"
        // someMethod()
    }

    /**
     * Тест демонстрирует базовый запуск корутины и обработку исключений.
     */
    @Test
    fun coro(): Unit = runBlocking { // 'runBlocking' — это билдер корутин. Он создает корутину
        // и БЛОКИРУЕТ текущий поток (в данном случае, поток теста)
        // до тех пор, пока все корутины внутри его блока не завершатся.
        // Это "мост" между миром корутин и обычным блокирующим кодом.

        launch { // 'launch' — еще один билдер. Он запускает новую корутину, которая выполняется
            // параллельно (конкурентно) с другим кодом в той же области видимости (scope).
            // Он не возвращает результат, а только 'Job' — объект, который представляет
            // фоновую задачу. Его можно использовать для отмены или ожидания завершения.
            try {
                // Вызываем наши suspend-функции последовательно.
                val str = someMethod()
                println("Apply: Получена строка '$str'")
                val len = str.length

                val num = otherMethod(len)
                println("Complete: Результат вычислений $num")
            } catch (ex: Exception) {
                // Исключения, возникшие внутри корутины, можно обрабатывать стандартным блоком try-catch.
                println("Exception: Перехвачено исключение $ex")
            }
        }.join() // '.join()' — это метод у объекта 'Job'. Он приостанавливает родительскую корутину
        // (ту, что в runBlocking) до тех пор, пока эта дочерняя корутина (запущенная через launch)
        // не завершится. Без .join() тест мог бы завершиться раньше, чем корутина успеет выполниться.

        println("Complete: Все корутины завершены, runBlocking разблокирован.")
    }

    // Функции для демонстрации работы с suspend-лямбдами
    fun doSomething(block: suspend (Int) -> Unit) { // Ожидает на вход suspend-лямбда-функцию
    }

    fun simpleMethod(i: Int) { // Обычная, не-suspend функция
    }

    /**
     * Тест демонстрирует тонкости работы с функциональными типами и suspend-функциями.
     */
    @Test
    fun signature(): Unit = runBlocking {
        val list = listOf(1, 2, 3)

        // Это работает, потому что 'forEach' является 'inline' функцией.
        // 'inline' функции подставляют свое тело напрямую в место вызова, поэтому компилятор
        // видит вызов 'otherMethod(it)' внутри корутины и корректно его обрабатывает.
        list.forEach { otherMethod(it) }

        // Это не сработало бы (если раскомментировать), потому что 'Consumer' - это не 'inline' интерфейс.
        // Его лямбда-представление не может быть suspend-функцией.
        // list.forEach (Consumer { otherMethod(it) })

        // 'doSomething' ожидает suspend-функцию, и мы передаем ей ссылку на 'otherMethod', которая является suspend.
        doSomething(::otherMethod)

        // 'doSomething' ожидает suspend-функцию, но мы передаем ссылку на обычную 'simpleMethod'.
        // Kotlin достаточно умен, чтобы автоматически обернуть вызов 'simpleMethod' в suspend-лямбду.
        doSomething(::simpleMethod)
    }

    /**
     * Тест показывает, как несколько корутин могут выполняться конкурентно на одном потоке.
     */
    @Test
    fun launch2(): Unit = runBlocking {
        launch { // Запускаем первую корутину
            for (i in 1..10) {
                println("coro1 $i")
                delay(100) // Приостанавливаем корутину, позволяя другой выполняться
            }
        }

        launch { // Запускаем вторую корутину
            for (i in 1..10) {
                println("coro2 $i")
                delay(110) // Небольшая разница в задержке для наглядности
            }
        }
        // Здесь нет .join(), потому что runBlocking будет ждать завершения всех дочерних корутин автоматически.
    }

    /**
     * Стресс-тест: запуск огромного количества корутин.
     * Демонстрирует легковесность корутин и важность потокобезопасности.
     */
    @Test
    fun launchMany(): Unit = runBlocking {
        // AtomicInteger - потокобезопасный класс для счетчиков. Операции с ним атомарны.
        val counter = AtomicInteger()
        // Обычная переменная Int НЕ потокобезопасна. К ней могут одновременно обращаться несколько потоков,
        // что приведет к "гонке данных" (race condition) и неверному итоговому значению.
        var dummyCounter = 0

        println("START: Запуск 1,000,001 корютин...")

        // 'newFixedThreadPoolContext' создает диспетчер корутин, который использует пул
        // из фиксированного количества потоков. Это "хрупкий" API, поэтому нужна аннотация.
        @OptIn(DelicateCoroutinesApi::class)
        newFixedThreadPoolContext(10, "fixed").use { fixThreadDispatcher ->
            // 'use' гарантирует, что пул потоков будет корректно закрыт после выполнения блока.

            launch { // Внешняя корутина для запуска всех остальных
                for (i in 0..1_000_000) {
                    // Запускаем внутреннюю корутину, которая будет выполняться в нашем пуле из 10 потоков.
                    launch(fixThreadDispatcher) {
                        delay(100)
                        // Атомарное увеличение счетчика. Потокобезопасно.
                        counter.incrementAndGet()
                        // НЕатомарное увеличение. Этот код подвержен гонке данных.
                        dummyCounter++
                    }
                }
            }.join() // Ждем завершения внешней корутины (и всех ее дочерних)
        }

        // Результат counter.get() будет всегда 1_000_001.
        // Результат dummyCounter будет почти всегда меньше, так как некоторые инкременты "потеряются" из-за гонки данных.
        println("COMPLETE: Атомарный счетчик = ${counter.get()},  Обычный счетчик = $dummyCounter")
    }

}