package ru.otus.otuskotlin.coroutines

import kotlinx.coroutines.*
import kotlin.coroutines.ContinuationInterceptor
import kotlin.test.Test

/**
 * Класс для демонстрации жизненного цикла и иерархии Job в корутинах.
 * Job — это обработчик фонового задания. Он может быть отменен,
 * и вы можете дождаться его завершения.
 */
class Ex5Job {

    /**
     * Вспомогательная функция-расширение для CoroutineScope.
     * Она запускает новую корутину, которая имитирует работу: выводит сообщение о старте,
     * "работает" в течение секунды и выводит сообщение о завершении.
     *
     * @param name Имя корутины для удобства отладки.
     * @param start Режим запуска корутины (по умолчанию немедленный).
     * @return Объект Job, представляющий эту корутину.
     */
    private fun CoroutineScope.createJob(name: String, start: CoroutineStart = CoroutineStart.DEFAULT) =
        launch(start = start) { // Запускаем новую корутину в текущем скоупе
            delay(10) // Небольшая задержка, чтобы в тестах успеть выполнить другие действия
            println("start $name") // Сообщение о начале работы
            delay(1000) // Имитация долгой задачи
            println("complete $name") // Сообщение о завершении
        }

    /**
     * Демонстрация метода .join()
     * .join() — это приостанавливающая функция, которая блокирует текущую корутину
     * до тех пор, пока корутина, у которой вызван .join(), не завершится.
     */
    @Test
    fun join(): Unit = runBlocking { // runBlocking создает корутину и блокирует поток теста до ее завершения
        val job1 = createJob("1") // Запускаем job1. Он начнет выполняться немедленно.
        job1.join() // Приостанавливаем runBlocking-корутину и ждем, пока job1 не завершится.
        println("After join") // Эта строка выполнится ТОЛЬКО после того, как job1 выведет "complete 1".
    }

    /**
     * Демонстрация ленивого запуска с CoroutineStart.LAZY.
     * Корутина не запускается сразу, а только при явном вызове .start() или .join().
     */
    @Test
    fun manualStart(): Unit = runBlocking {
        // Создаем job1, но НЕ запускаем его. Он находится в "созданном" состоянии.
        val job1 = createJob("1", start = CoroutineStart.LAZY)
        delay(50) // Ждем 50мс. В этот момент job1 все еще не запущен, сообщения "start 1" не будет.
        println("Manual start")
        job1.start() // Явно запускаем выполнение корутины job1.
        // runBlocking будет ждать завершения job1, так как это дочерняя корутина.
    }

    /**
     * Что произойдет, если вызвать .join() у LAZY корутины без .start()?
     * Ответ: .join() сам запустит корутину и будет ждать её завершения.
     */
    @Test
    fun dontStart(): Unit = runBlocking {
        // Создаем "ленивую" корутину.
        val job1 = createJob("1", start = CoroutineStart.LAZY)
        delay(50) // Ждем. Корутина все еще не запущена.
        job1.join() // Вызов join() у LAZY корутины запускает её и дожидается завершения.
        // Вывод будет: "start 1", "complete 1", и только потом тест завершится.
    }

    /**
     * Демонстрация отмены корутины с помощью .cancel().
     * Отмена — это кооперативный процесс: сама корутина должна periodically проверять, не была ли она отменена.
     * Функции из библиотеки coroutines (например, delay) делают это автоматически.
     */
    @Test
    fun cancel(): Unit = runBlocking {
        val job1 = createJob("1") // Запускаем корутину
        delay(50) // Ждем, чтобы корутина успела напечатать "start 1"
        job1.cancel() // Отправляем сигнал отмены.
        println("After cancel") // Эта строка выполнится сразу после отправки сигнала.
        // Сообщение "complete 1" НЕ будет напечатано, так как delay(1000) внутри корутины
        // проверит статус отмены и выбросит CancellationException, которое корректно обработается.
    }

    /**
     * Проблема с отменой: использование блокирующих операций.
     * Если корутина выполняет блокирующий код (например, Thread.sleep), она не может быть отменена,
     * так как не проверяет свой статус.
     */
    @Suppress("RedundantSuspendModifier")
    private suspend fun x() {
        @Suppress("BlockingMethodInNonBlockingContext")
        Thread.sleep(10) // Блокирующая операция! Она не "слушает" сигналы отмены.
    }

    @Test
    fun cancelTrouble(): Unit = runBlocking(Dispatchers.Default) {
        val job1 = launch {
            for (i in 1..1000) {
                x() // Вызываем блокирующую функцию
                // Решение: нужно явно проверять статус корутины.
                //if (!isActive) break // isActive — свойство, доступное внутри корутины, которое показывает её активность.
            }
            println("Job complete") // Это сообщение будет напечатано, несмотря на cancel()
        }
        delay(50)
        println("Before cancel")
        job1.cancel() // Отправляем сигнал отмены, но корутина его "не слышит" из-за Thread.sleep.
        println("After cancel")
        // Результат: корутина продолжит выполняться и напечатает "Job complete".
    }

    /**
     * Демонстрация CoroutineScope и структурированного параллелизма.
     * Отмена скоупа отменяет все его дочерние корутины.
     */
    @Test
    fun scope() {
        // Создаем новый скоуп с собственным Job. Этот Job будет "родителем" для всех корутин в этом скоупе.
        val scope = CoroutineScope(Job())
        scope.createJob("1") // Запускаем дочернюю корутину
        scope.createJob("2") // Запускаем вторую дочернюю корутину

        Thread.sleep(500) // Ждем некоторое время
        scope.cancel() // Отменяем родительский скоуп. Это автоматически отменит job1 и job2.
        // Сообщения "complete 1" и "complete 2" не будут напечатаны.
    }

    /**
     * Демонстрация иерархии Job и контекста корутины.
     * Каждая корутина имеет свой контекст, который включает в себя Job.
     * Родительская корутина ждет завершения всех дочерних.
     */
    @Test
    fun scopeHierarchy(): Unit = runBlocking {
        // 'this' здесь ссылается на CoroutineScope runBlocking
        println("top $this")
        val job1 = launch { // job1 является дочерней для runBlocking
            // 'this' здесь ссылается на CoroutineScope job1
            println("job1 block $this")

            // Получаем Job из контекста текущей корутины (job1)
            val myJob = this.coroutineContext[Job]
            println("job1 myJob $myJob")

            val job2 = launch { // job2 является дочерней для job1
                // 'this' здесь ссылается на CoroutineScope job2
                println("job2 block $this")
            }
            println("job2 $job2") // Выводим объект job2
        }
        println("job1 $job1") // Выводим объект job1
    }
}