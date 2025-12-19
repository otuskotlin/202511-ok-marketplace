package ru.otus.otuskotlin.coroutines

import kotlinx.coroutines.*
import kotlin.concurrent.thread
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.test.Test

/**
 * Класс для демонстрации работы различных диспетчеров (Dispatchers) в Kotlin Coroutines.
 * Диспетчер определяет, в каком потоке или пуле потоков будет выполняться корутина.
 */
class Ex9Dispatchers {

    /**
     * Вспомогательная функция, которая запускает 30 корутин.
     * Каждая корутина блокирует свой поток на 500 миллисекунд с помощью `Thread.sleep()`.
     * Это позволяет наглядно увидеть, как разные диспетчеры справляются с большим количеством блокирующих задач.
     */
    private fun CoroutineScope.createCoro() {
        repeat(30) { i ->
            launch {
                println("coroutine $i, start in thread ${Thread.currentThread().name}")
                // ВАЖНО: Thread.sleep() — это БЛОКИРУЮЩАЯ операция.
                // Она блокирует весь поток, в котором выполняется.
                // В реальном коде следует использовать suspend-функцию delay().
                Thread.sleep(500)
                println("coroutine $i, end in thread ${Thread.currentThread().name}")
            }
        }
    }

    /**
     * Тест с диспетчером по умолчанию (Dispatchers.Default).
     * runBlocking по умолчанию использует этот диспетчер для своих дочерних корутин.
     * Dispatchers.Default оптимизирован для CPU-интенсивных задач и использует общий пул потоков (ForkJoinPool).
     */
    @Test
    fun default() = runBlocking {
        // Корутины будут выполняться в пуле потоков по умолчанию.
        // Количество одновременно работающих потоков будет ограничено размером этого пула
        // (обычно равно количеству ядер CPU).
        createCoro()
    }

    /**
     * Тест с диспетчером Dispatchers.IO.
     * Этот диспетчер оптимизирован для блокирующих операций ввода-вывода (сеть, диск).
     * Он использует больший пул потоков и может создавать новые потоки по мере необходимости,
    // чтобы справиться с большим количеством блокируемых задач.
     */
    @Test
    fun io() = runBlocking {
        // withContext переключает контекст выполнения на Dispatchers.IO.
        // Все корутины, запущенные внутри этого блока, будут использовать его пул потоков.
        // Вы увидите, что количество задействованных потоков будет значительно больше,
        // чем в случае с Dispatchers.Default.
        withContext(Dispatchers.IO) {
            createCoro()
        }
    }

    /**
     * Тест с пользовательским диспетчером.
     * Мы можем создать свой собственный пул потоков с заданным размером.
     */
    @Test
    fun custom() = runBlocking {
        // Создаем диспетчер на основе Java ExecutorService с пулом из 8 потоков.
        // `newFixedThreadPoolContext` - это удобный способ сделать это.
        @OptIn(DelicateCoroutinesApi::class)
        val dispatcher = newFixedThreadPoolContext(8, "my-custom-pool")

        // `dispatcher.use { ... }` гарантирует, что пул потоков будет корректно закрыт
        // после завершения блока, предотвращая утечки ресурсов.
        dispatcher.use {
            // Переключаемся на наш пользовательский диспетчер.
            // Все 30 корутин будут конкурировать за эти 8 потоков.
            withContext(Job() + dispatcher) {
                createCoro()
            }
        }
    }

    /**
     * Тест, демонстрирующий поведение Dispatchers.Unconfined и механизм `suspendCoroutine`.
     * `Dispatchers.Unconfined` не привязывает корутину к конкретному потоку.
     */
    @Test
    fun unconfined(): Unit = runBlocking(Dispatchers.Default) {
        // Переключаемся на Unconfined. Код внутри начнет выполняться в текущем потоке
        // (который является потоком из пула Dispatchers.Default).
        withContext(Dispatchers.Unconfined) {
            launch {
                // Эта корутина также начнет выполняться в потоке Dispatchers.Default.
                println("start coroutine in thread ${Thread.currentThread().name}")

                // `suspendCoroutine` — это мост между миром корутин и миром колбэков.
                // Она приостанавливает корутину и передает в лямбду объект `Continuation` (здесь он называется `it`).
                // Корутина будет "спать", пока не будет вызван `it.resume()` или `it.resumeWithException()`.
                suspendCoroutine { continuation ->
                    println("suspend function, start in thread ${Thread.currentThread().name}")
                    // Внутри приостановки мы запускаем обычный, не-корутинный поток (OS thread).
                    thread {
                        println("suspend function, background work in thread ${Thread.currentThread().name}")
                        Thread.sleep(1000)
                        // Когда работа в фоновом потоке завершена, мы возобновляем корутину.
                        // Ключевой момент: корутина возобновится в том потоке, который вызвал `resume`.
                        continuation.resume("Data!")
                    }
                }
                // Этот код выполнится после вызова `continuation.resume()`.
                // Он будет выполняться в фоновом потоке, который мы создали выше.
                println("end coroutine in thread ${Thread.currentThread().name}")
            }
        }
    }
}