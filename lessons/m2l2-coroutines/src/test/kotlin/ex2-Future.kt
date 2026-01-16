package ru.otus.otuskotlin.coroutines

import java.util.concurrent.CompletableFuture
import kotlin.test.Test

/**
 * Класс для демонстрации работы с CompletableFuture.
 * CompletableFuture — это класс в Java для асинхронного программирования,
 * который позволяет строить цепочки зависимых вычислений.
 */
class Ex2Future {

    /**
     * Вспомогательный метод, который асинхронно выполняет задачу и возвращает CompletableFuture<String>.
     *
     * @return CompletableFuture, который по завершении будет содержать строку "Some data".
     */
    private fun someMethod(): CompletableFuture<String> = CompletableFuture.supplyAsync {
        // Этот блок кода будет выполнен асинхронно в отдельном потоке из пула потоков по умолчанию (ForkJoinPool.commonPool()).
        println("Some method: Начало асинхронной операции")
        Thread.sleep(1000) // Имитация долгой операции (например, сетевой запрос).
        // throw RuntimeException("Error") // Раскомментируйте для симуляции ошибки в someMethod.
        "Some data" // Результат, который будет "упакован" в CompletableFuture по завершении.
    }

    /**
     * Еще один вспомогательный асинхронный метод.
     *
     * @param a число, которое будет удвоено.
     * @param throwPlace параметр для управления местом возникновения исключения:
     *        1 - исключение будет выброшено синхронно, до запуска асинхронной задачи.
     *        2 - исключение будет выброшено асинхронно, внутри задачи.
     *        0 (по умолчанию) - исключения не будет.
     * @return CompletableFuture, который по завершении будет содержать результат (a * 2).
     */
    private fun otherMethod(a: Int, throwPlace: Int = 0): CompletableFuture<Int> {
        // Синхронная проверка. Исключение здесь прервет выполнение цепочки до запуска supplyAsync.
        if (throwPlace == 1) throw RuntimeException("Ошибка 1: Синхронное исключение в otherMethod")

        // supplyAsync запускает асинхронную задачу.
        return CompletableFuture.supplyAsync {
            println("Other method: Начало асинхронной операции с числом $a")
            // Асинхронная проверка. Исключение здесь будет перехвачено внутри цепочки CompletableFuture.
            if (throwPlace == 2) throw RuntimeException("Ошибка 2: Асинхронное исключение в otherMethod")
            Thread.sleep(1000) // Имитация долгой операции.
            a * 2 // Возвращаемый результат.
        }
    }

    /**
     * Тест демонстрирует "счастливый путь" (happy path) — последовательное выполнение асинхронных операций.
     */
    @Test
    fun future() {
        someMethod() // 1. Запускаем первую асинхронную операцию. Получаем CompletableFuture<String>.
            .thenApply {
                // 2. thenApply: Этот оператор будет выполнен, когда someMethod() завершится УСПЕШНО.
                // Он получает результат предыдущего этапа (строку "Some data" в переменной 'it') и преобразует его.
                // Тип результата меняется с String на Int (длина строки).
                println("thenApply: Преобразование результата '${it}' в его длину.")
                it.length
            }
            .thenCompose {
                // 3. thenCompose: Используется, когда следующая операция сама возвращает CompletableFuture.
                // Он "распаковывает" вложенный CompletableFuture, чтобы избежать CompletableFuture<CompletableFuture<Int>>.
                // Он получает результат предыдущего этапа (Int) и вызывает следующую асинхронную операцию.
                println("thenCompose: Запуск otherMethod с результатом $it")
                otherMethod(it)
            }
            .handle { num, ex ->
                // 4. handle: Это терминальный оператор, который обрабатывает и успешный результат, и исключение.
                // Он всегда выполняется. 'num' - это результат, если все прошло успешно.
                // 'ex' - это исключение, если оно произошло на любом из предыдущих этапов.
                // Один из этих параметров будет null.
                if (ex != null) {
                    println("handle: Произошло исключение! $ex")
                } else {
                    println("handle: Операция завершена успешно. Результат: $num")
                }
                // Возвращаемое значение здесь будет результатом всего CompletableFuture. В данном случае это Unit (Void).
            }
            .get() // 5. get(): БЛОКИРУЮЩИЙ вызов. Он ждет, пока вся цепочка вызовов не завершится.
        // Используется в тестах для того, чтобы дождаться результата. В реальном приложении
        // блокировку следует избегать.

        println("Complete: Основной поток продолжил выполнение после .get()")

        // Thread.sleep(3000) // Альтернатива .get() - просто подождать, пока асинхронные задачи завершатся.
    }

    /**
     * Тест демонстрирует обработку синхронного исключения, возникшего внутри лямбда-выражения.
     */
    @Test
    fun exception() {
        // 1. Создаем уже завершенный CompletableFuture с начальным значением 42.
        CompletableFuture.completedFuture(42)
            .thenCompose {
                // 2. thenCompose запускается, так как первый CompletableFuture уже завершен.
                try {
                    // 3. Вызываем otherMethod с параметром throwPlace = 1.
                    // Это приведет к СИНХРОННОМУ исключению RuntimeException еще до запуска асинхронной части.
                    otherMethod(it, 1)
                } catch (e: Exception) {
                    // 4. Синхронное исключение перехватывается обычным блоком try-catch.
                    println("thenCompose: Перехвачено синхронное исключение $e")
                    // Важно: после обработки исключения мы должны вернуть CompletableFuture,
                    // чтобы продолжить цепочку. Возвращаем новый, уже завершенный CompletableFuture.
                    CompletableFuture.completedFuture(42)
                }
            }
            .get() // 5. Ждем завершения цепочки.

        println("Complete: Основной поток продолжил выполнение.")
    }
}