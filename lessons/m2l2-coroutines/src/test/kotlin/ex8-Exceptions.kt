package ru.otus.otuskotlin.coroutines

import kotlinx.coroutines.*
import kotlin.test.Test

/**
 * Класс для демонстрации различных сценариев обработки исключений в корутинах.
 * Ключевые темы: propagation (распространение) исключений, CoroutineExceptionHandler,
 * отмена скоупа и SupervisorJob.
 */
class Ex8Exceptions {

    /**
     * Тест показывает НЕПРАВИЛЬНЫЙ способ обработки исключений.
     * Блок try-catch не может поймать исключение, которое происходит в другой, конкурентно запущенной корутине.
     */
    @Test
    fun invalid() {
        try {
            val scope = CoroutineScope(Dispatchers.Default)
            // launch запускает корутину "в огне и забыть" (fire-and-forget).
            // Исключение внутри неё происходит в асинхронном контексте и не попадает
            // во внешний блок try-catch.
            scope.launch {
                // Этот код вызовет NumberFormatException
                Integer.parseInt("a")
            }
        } catch (e: Exception) {
            // Эта строка НИКОГДА не выполнится.
            println("CAUGHT!")
        }

        Thread.sleep(2000)
        println("COMPLETED!")
        // Несмотря на отлов исключения, программа не "упадет". Uncaught exception в корутине
        // будет передан в Thread.defaultUncaughtExceptionHandler, который по умолчанию просто печатает
        // стек-трейс в stderr.
    }

    /**
     * Еще один НЕПРАВИЛЬНЫЙ способ.
     * Исключение не "всплывает" из дочерней корутины в родительскую.
     */
    @Test
    fun invalid2() {
        val scope = CoroutineScope(Dispatchers.Default)
        scope.launch { // Это родительская корутина
            try {
                launch { // Это дочерняя корутина. У нее свой собственный контекст выполнения.
                    Integer.parseInt("a")
                }
            } catch (e: Exception) {
                // Этот catch тоже не сработает, потому что исключение происходит
                // в дочерней корутине, а не в блоке try родительской.
                println("CAUGHT!")
            }
        }

        Thread.sleep(2000)
        println("COMPLETED!")
    }

    /**
     * Вспомогательная функция для создания CoroutineExceptionHandler.
     * Это специальный элемент контекста, который может "ловить" необработанные исключения
     * в корутинах и их дочерних элементах.
     */
    private fun handler(where: String) = CoroutineExceptionHandler { context, exception ->
        // Лямбда принимает контекст корутины, в которой произошла ошибка, и само исключение.
        println("CAUGHT at $where ${context[CoroutineName]}: $exception")
    }

    /**
     * Демонстрация правильного способа обработки исключений на уровне скоупа.
     */
    @Test
    fun handler() {
        // Создаем скоуп, в контекст которого добавлен наш обработчик.
        val scope = CoroutineScope(Dispatchers.Default + handler("top"))
        scope.launch(handler("launch")) { // Можно добавить обработчик и на конкретную корутину
            Integer.parseInt("a")
        }

        Thread.sleep(2000)
        println("COMPLETED!")
        // Исключение будет поймано обработчиком, который находится ближе к корутине в иерархии контекста.
        // В данном случае, это будет "launch" handler.
    }

    /**
     * Демонстрация иерархии обработчиков.
     */
    @Test
    fun handler2() {
        val scope = CoroutineScope(Dispatchers.Default + handler("top"))
        scope.launch(CoroutineName("1")) {
            // Создаем дочернюю корутину со своим обработчиком и именем.
            launch(handler("child") + CoroutineName("1.1")) {
                Integer.parseInt("a")
            }
        }

        Thread.sleep(2000)
        println("COMPLETED!")
        // Исключение произойдет в корутине "1.1". Сначала будет проверен её контекст.
        // Там есть handler("child"), поэтому он и поймает исключение.
        // Обработчик "top" не будет вызван.
    }

    /**
     * Демонстрация поведения по умолчанию: "отказаться при первой ошибке" (fail-fast).
     * Необработанное исключение в дочерней корутине отменяет её родителя,
     * что, в свою очередь, отменяет всех остальных дочерних корутин этого родителя (сиблингов).
     */
    @Test
    fun cancel() {
        val scope = CoroutineScope(Dispatchers.Default + handler("top"))
        scope.launch {
            launch { // 3. Эта корутина - сиблинг для той, что ниже. Она тоже будет отменена.
                delay(100)
                println("cor3")
            }
            launch { // Родительская корутина для cor1 и cor2.
                launch { // 1. Дочерняя корутина. Будет отменена из-за ошибки в родителе.
                    delay(100)
                    println("cor1")
                }
                launch { // 2. Еще одна дочерняя корутина. Тоже будет отменена.
                    delay(100)
                    println("cor2")
                }

                // 3. Здесь происходит исключение. Оно отменяет текущую корутину (родителя для cor1/cor2).
                Integer.parseInt("a")
            }
        }

        Thread.sleep(2000)

        scope.launch {
            // 4. Эта корутина не запустится, потому что скоуп будет отменен после первого исключения.
            println("No chancel")
        }

        // Другие скоупы не затрагиваются.
        val scope2 = CoroutineScope(Dispatchers.Default)
        scope2.launch {
            println("I am alive")
        }

        Thread.sleep(500)

        println("COMPLETED!")
    }

    /**
     * SupervisorJob решает проблему "fail-fast".
     * Он отменяет только ту ветку, где произошла ошибка, но не трогает сиблингов.
     */
    @Test
    fun supervisorJob() {
        // SupervisorJob в корне скоупа меняет поведение по умолчанию.
        val scope = CoroutineScope(Dispatchers.Default + handler("top") + SupervisorJob())
        scope.launch {
            launch { // 1. Эта корутина-сиблинг НЕ будет отменена.
                delay(100)
                println("cor1")
            }
            launch { // 2. И эта тоже НЕ будет отменена.
                delay(100)
                println("cor2")
            }
            launch { // 3. Корутина, в которой произойдет ошибка.
                delay(50)
                Integer.parseInt("a")
            }

            delay(100)
            // 1. Сама эта корутина (родитель для cor1, cor2, ...) будет отменена,
            // так как одна из её дочерних задач завершилась с ошибкой.
            println("super")
        }

        Thread.sleep(2000)

        // 3. Скоуп жив, так как ошибка была изолирована внутри дочерних корутин SupervisorJob'а.
        // Поэтому можно запускать новые задачи.
        scope.launch {
            println("I am alive")
        }

        Thread.sleep(500)

        println("COMPLETED!")
    }

    /**
     * SupervisorJob можно применять не ко всему скоупу, а к отдельной ветке.
     */
    @Test
    fun supervisorJob2() {
        val scope = CoroutineScope(Dispatchers.Default + handler("top") + SupervisorJob())
        scope.launch { // *** Эта корутина имеет обычный Job, а не SupervisorJob.
            launch { // Сиблинг для корутины с SupervisorJob.
                delay(100)
                println("cor1")
            }
            // ОПАСНО: SupervisorJob() создан без родителя.
            // Эта корутина становится "сиротой".
            launch(SupervisorJob()) { // Внутри этой корутины создаем новый SupervisorJob.
                // Ошибка здесь отменит только эту корутину, но не её сиблингов.
                delay(50)
                Integer.parseInt("a") // 1 - если закомментировать, то "cor1" и "super" напечатаются.
                println("cor2")
            }

            delay(100)
            // 1. Так как родительский Job не SupervisorJob, ошибка в дочерней корутине
            // (даже с собственным SupervisorJob) отменит эту родительскую корутину.
            println("super")
        }

        // scope.cancel() // 1 - если раскомментировать, все будет отменено.

        Thread.sleep(2000)

        println("COMPLETED!")
    }

    /**
     * SupervisorJob, который является дочерним для другого Job.
     * Это позволяет изолировать сбои внутри поддерева, но не отменяет родительскую корутину.
     */
    @Test
    fun supervisorJob3() {
        val scope = CoroutineScope(Dispatchers.Default + handler("top"))
        scope.launch { // *** Родительская корутина.
            launch { // Сиблинг.
                delay(100)
                println("cor1")
            }
            // Создаем SupervisorJob, который является дочерним для coroutineContext.job (т.е. для текущей launch).
            launch(SupervisorJob(coroutineContext.job)) {
                launch { // Внучатая корутина, в которой произойдет ошибка.
                    delay(10)
                    Integer.parseInt("a") // 1 - если закомментировать, то "cor2" и "cor3" напечатаются.
                }
                launch { // Сиблинг для той, что выше. Он будет отменен.
                    delay(50)
                    println("cor2")
                }
                delay(50)
                // Эта корутина (родитель для двух launch внутри) тоже будет отменена.
                println("cor3")
            }

            delay(100)
            // 1. Эта родительская корутина НЕ будет отменена, потому что сбой произошел
            // в поддереве с SupervisorJob, который изолировал сбой.
            println("super")
        }

        // scope.cancel() // 1 - если раскомментировать, все будет отменено.

        Thread.sleep(2000)

        println("COMPLETED!")
    }

    /**
     * CoroutineExceptionHandler все равно будет вызван, даже при использовании SupervisorJob.
     */
    @Test
    fun handler3() {
        val scope = CoroutineScope(Dispatchers.Default + handler("top"))
        scope.launch(CoroutineName("1")) {
            launch(handler("child") + CoroutineName("1.1") + SupervisorJob(coroutineContext.job)) {
                Integer.parseInt("a")
            }
        }

        Thread.sleep(2000)
        println("COMPLETED!")
        // Исключение будет поймано обработчиком "child", так как он находится ближе всего.
    }

    /**
     * Исключения в async не распространяются до вызова .await().
     */
    @Test
    fun async1() {
        val scope = CoroutineScope(Dispatchers.Default)
        scope.launch {
            launch {
                delay(100)
                println("cor1") // Эта корутина не будет отменена.
            }
            val x = async {
                Integer.parseInt("a") // Исключение произойдет, но не "всплывет" пока не вызовем await()
            }

            delay(100)

            println("1")
            try {
                x.await() // Вот здесь исключение будет выброшено и поймано.
            } catch (e: Exception) {
                println("CAUGHT!")
            }
        }

        Thread.sleep(2000)

        println("COMPLETED!")
    }

    /**
     * SupervisorJob для async.
     * Полезно, когда вы хотите, чтобы сбой в одной async-задаче не отменил другие.
     */
    @Test
    fun async2() {
        val scope = CoroutineScope(Dispatchers.Default)
        scope.launch {
            launch {
                delay(100)
                println("cor1")
            }
            // Используем SupervisorJob, чтобы сбой в async не отменил родительскую корутину.
            val x = async(SupervisorJob(coroutineContext.job) + handler("async")) {
                Integer.parseInt("a")
            }

            delay(100)

            println("1")
            try {
                x.await()
            } catch (e: Exception) {
                println("CAUGHT!") // Исключение будет поймано здесь.
                // Если бы не было try-catch, оно было бы поймано handler("async").
            }
        }

        Thread.sleep(2000)

        println("COMPLETED!")
    }
}
