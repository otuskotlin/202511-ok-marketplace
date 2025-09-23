package ru.otus.otuskotlin.flows

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.*
import org.junit.Assert
import org.junit.Assert.assertThrows
import org.slf4j.LoggerFactory
import java.util.*
import kotlin.concurrent.scheduleAtFixedRate
import kotlin.test.Test
import kotlin.test.assertEquals

class FlowTest {

    suspend fun getWikiPages(): List<String> {
        delay(3000)
        return listOf("one", "two", "three")
    }

    fun getWikiPagesFlow(): Flow<String> = flow {
        delay(1000)
        emit("one")
        delay(1000)
        emit("two")
        delay(1000)
        emit("three")
    }

    /**
     * Отличия flow от suspend-функции, возвращающей список
     */
    @Test
    fun flow(): Unit = runBlocking {
        logger.info("Start getWikiPages")
        val result = getWikiPages()
        logger.info("Complete: {}", result)

        logger.info("Start getWikiPages2-flow")
        getWikiPagesFlow().collect {
            logger.info("Result: {}", it)
        }
        logger.info("Complete")
    }

    /**
     * Некоторые операторы
     */
    @Test
    fun operators(): Unit = runBlocking {
        flowOf(1, 2, 3, 4) // билдер
            .onEach { println(it) } // операции ...
            .map { it + 1 }
            .filter { it % 2 == 0 }
            .collect { println("Result number $it") } // терминальный оператор
    }


    /**
     * Хелпер-функция для печати текущего потока
     */
    private fun <T> Flow<T>.printThreadName(msg: String) =
        this.onEach { println("Msg = $msg, thread name = ${Thread.currentThread().name}") }

    /**
     * Демонстрация переключения корутин-контекста во flow
     */
    @OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
    @Test
    fun coroutineContextChange(): Unit = runBlocking {
        // Просто создали диспетчера и безопасно положили его в apiDispatcher
        newSingleThreadContext("Api-Thread").use { apiDispatcher ->
            // еще один...
            newSingleThreadContext("Db-Thread").use { dbDispatcher ->

                // Контекст переключается в ОБРАТНОМ ПОРЯДКЕ, т.е. СНИЗУ ВВЕРХ
                flowOf(10, 20, 30) // apiDispatcher
                    .filter { it % 2 == 0 } // apiDispatcher
                    .map {
                        delay(2000)
                        it
                    } // apiDispatcher
                    .printThreadName("api call") // apiDispatcher
                    .flowOn(apiDispatcher) // Переключаем контекст выполнения на apiDispatcher
                    .map { it + 1 } // dbDispatcher
                    .printThreadName("db call") // dbDispatcher
                    .flowOn(dbDispatcher) // Переключаем контекст выполнения на dbDispatcher
                    .printThreadName("last operation") // Default
                    .onEach { println("On each $it") } // Default
                    .collect() // Запустится в контексте по умолчанию, т.е. в Dispatchers.Default
            }
        }
    }

    @Test
    fun flowIsCold(): Unit = runBlocking {
        val flow = flowOf(1, 2, 3)
            .onEach { println("onEach $it") }

        flow.collect() // collect может не содержать действия
        println("-------------")
        flow.collect() // - это удобно, если нам нужен побоный эффект от самого факта запуска
    }

    /**
     * Демонстрация тригеров onStart, onCompletion, catch, onEach
     */
    @Test
    fun startersStopers(): Unit = runBlocking {
        flow {
            while (true) {
                emit(1)
                delay(1000)
                emit(2)
                delay(1000)
                emit(3)
                delay(1000)
                throw RuntimeException("Custom error!")
            }
        }
            .onStart { println("On start") } // Запустится один раз только вначале
            .onCompletion { println(" On completion") } // Запустится один раз только вконце
            .catch { println("Catch: ${it.message}") } // Запустится только при генерации исключения
            .onEach { println("On each: $it") } // Генерируется для каждого сообщения
            .collect { }
    }


    @Test
    fun flowBuilders() = runBlocking {
        println("пустой поток")
        emptyFlow<Int>().collect { println(it) }

        println("Поток из набора элементов")
        flowOf(1, 2, 3).collect { println(it) }

        println("Ленивый билдер")
        flow {
            emit(1)
            emit(2)
            delay(1000)
            emit(3)
        }.collect { println(it) }

        println("Поток из коллекции")
        listOf(1, 2, 3).asFlow().collect { println(it) }

        println("Поток из последовательности")
        sequenceOf(1, 2, 3).asFlow().collect { println(it) }

        assertThrows(RuntimeException::class.java) {
            runBlocking {
                flow {
                    launch { emit(1) } // запрещено вызывать emit из новых корутин!
                }.collect { println(it) }
            }
        }

        println("Контекстно- и потоко-безопасный билдер")
        channelFlow {
            launch { send("foo") } // а внутри channelFlow - можно
            delay(100)
            send("bar")
        }.collect { println(it) }

        println("callbackFlow")
        callbackFlow {
            val timer = Timer()
            timer.scheduleAtFixedRate(delay = 0L, period = 1000L) {
                trySendBlocking("text")
            }
            awaitClose { timer.cancel() }
        }.take(3).collect { println(it) }
    }


    @Test
    fun operators2() = runBlocking {

        val flow = flow {
            emit(1)
            emit(2)
            delay(300)
            emit(3)
            emit(4)
            emit(5)
        }

        flow
            .onEach { logger.info("Got Item: $it") }
            .onStart { logger.info("Поток стартовал") }
            .onCompletion { logger.info("Поток завершен") }

        println("Пример трансформации")
        flow
            .map { it * 2 }
            .filter { it != 4 }
            .take(3)
            .drop(1)
            .collect { println("Преобразован: $it") }

        flow
            .buffer(128, BufferOverflow.DROP_OLDEST)
            .onEach { delay(100) }
            .collect { println("Буфф. элем: $it") }

        // Пример throttling
        flow
            .debounce(200) // Устанавливаем паузу между элементами
            .collect { println("Debounced item: $it") }

        println("\n--- Пример retry ---")

        // Пример retry
        flow {
            emit(1)
            throw RuntimeException("Test Exception")
        }
            .retry(3) { throwable ->
                println("Retry on error: ${throwable.message}")
                true // Повторяем поток на каждой ошибке
            }
            .catch { println("Caught error: ${it.message}") }
            .collect()

        println("\n--- Пример смены контекста ---")

        // Пример смены контекста
        flow
            .map { it * 2 }
            .flowOn(Dispatchers.IO)
            .collect { println("Processed on IO: $it") }
    }

    /**
     * Демонстрация реализации кастомного оператора для цепочки.
     */
    @Test
    fun customOperator(): Unit = runBlocking {
        fun <T> Flow<T>.zipWithNext(): Flow<Pair<T, T>> = flow {
            var prev: T? = null
            collect { el ->
                prev?.also { pr -> emit(pr to el) } // Здесь корректная проверка на NULL при использовании var
                prev = el
            }
        }

        flowOf(1, 2, 3, 4)
            .zipWithNext()
            .collect { println(it) }
    }

    /**
     * Терминальный оператор toList.
     * Попробуйте другие: collect, toSet, first, single (потребуется изменить билдер)
     */
    @Test
    fun toListTermination(): Unit = runBlocking {
        val list = flow {
            emit(1)
            delay(100)
            emit(2)
            delay(100)
        }
            .onEach { println("$it") }
            .toList()

        println("List: $list")
    }

    /**
     * Работа с бесконечными билдерами flow
     */
    @Test
    fun infiniteBuilder(): Unit = runBlocking {
        val list = flow {
            var index = 0
            // здесь бесконечный цикл, не переполнения не будет из-за take
            while (true) {
                emit(index++)
                delay(100)
            }
        }
            .onEach { println("$it") }
            .take(10) // Попробуйте поменять аргумент и понаблюдайте за размером результирующего списка
            .toList()

        println("List: $list")
    }

}