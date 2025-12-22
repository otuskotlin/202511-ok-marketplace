package ru.otus.otuskotlin.flows

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.test.Test

class FlowSharedTest {

    /**
     * Пример работы SharedFlow.
     * SharedFlow — это "горячий" (hot) поток. Это значит, что он активно существует в памяти
     * и излучает значения независимо от того, есть ли у него подписчики.
     * Когда значение излучается (emit), его получают ВСЕ активные подписчики.
     */
    @Test
    fun shared(): Unit = runBlocking {
        // Создаем "горячий" изменяемый поток. Он может как излучать значения, так и иметь подписчиков.
        val shFlow = MutableSharedFlow<String>()

        // Запускаем первую корутину-подписчик. Она будет собирать все значения, которые излучит shFlow.
        // Подписчики сами по себе никогда не завершатся, так как SharedFlow не имеет конечной точки.
        launch { shFlow.collect { println("XX $it") } }

        // Запускаем вторую корутину-подписчик. Она также будет получать все значения.
        launch { shFlow.collect { println("YY $it") } }

        // Начинаем излучать значения в поток.
        (1..10).forEach {
            delay(100)
            shFlow.emit("number $it") // Каждое emit будет получено ОБЕИМИ подписчиками.
        }

        // Поскольку подписчики (collect) работают вечно, нам нужен способ их остановить,
        // чтобы тест мог завершиться. cancelChildren отменяет все дочерние корутины,
        // запущенные в этом runBlocking-скоупе.
        coroutineContext.cancelChildren()
    }

    /**
     * Демонстрация правильного разделения ролей: публикации данных и их получения (подписки).
     * Это важный паттерн для инкапсуляции. Мы не хотим, чтобы внешние компоненты могли
     * произвольно изменять состояние потока.
     */
    @Test
    fun collector(): Unit = runBlocking {
        // 1. Создаем изменяемый SharedFlow. Он будет "владеть" потоком и излучать данные.
        val mshFlow = MutableSharedFlow<String>()

        // 2. Создаем его НЕИЗМЕНЯЕМУЮ (read-only) версию. Эту ссылку мы можем безопасно
        // передавать "наружу" для подписки. Внешний код не сможет вызвать .emit() на ней.
        val shFlow = mshFlow.asSharedFlow()

        // 3. FlowCollector — это еще один способ получить ссылку для публикации данных.
        // Он предоставляет доступ только к функции .emit().
        val collector: FlowCollector<String> = mshFlow

        // Подписчик №1: подписывается напрямую на изменяемый поток.
        launch {
            mshFlow.collect {
                println("MUT $it")
            }
        }

        // Подписчик №2: подписывается на неизменяемую (публичную) версию потока.
        launch {
            shFlow.collect {
                println("IMMUT $it")
            }
        }

        delay(100) // Даем подписчикам время на подключение

        // Излучаем данные, используя ссылку collector.
        (1..20).forEach {
            collector.emit("zz: $it")
        }

        delay(1000)
        coroutineContext.cancelChildren()
    }

    /**
     * Пример конвертации "холодного" Flow в "горячий" SharedFlow.
     * Это полезно, когда у вас есть источник данных, который должен быть активен
     * независимо от количества подписчиков (например, поток данных с сервера).
     */
    @Test
    fun otherShared(): Unit = runBlocking {
        // Это "холодный" поток. Каждый раз, когда на него подписываются, его код выполняется заново.
        val coldFlow = flowOf(100, 101, 102, 103, 104, 105).onEach { println("Cold: $it") }

        // Запускаем двух подписчиков на холодный поток.
        // Мы увидим, что "Cold: $it" напечатается дважды, так как поток запустится для каждого подписчика отдельно.
        launch { coldFlow.collect() }
        launch { coldFlow.collect() }

        // А теперь создадим "горячий" поток на основе холодного.
        val hotFlow = flowOf(200, 201, 202, 203, 204, 205)
            .onEach { println("Hot: $it") }
            // .shareIn() — это оператор, который превращает Flow в SharedFlow.
            // 1. `this` — это скоуп, в котором будет работать горячий поток.
            // 2. `SharingStarted.Lazily` — стратегия запуска. "Ленивая" означает, что
            //    upstream-поток (flowOf...) начнет выполняться только тогда, когда
            //    появится первый подписчик, и остановится, когда подписчики уйдут.
            .shareIn(this, SharingStarted.Lazily)

        // Запускаем двух подписчиков на горячий поток.
        // "Hot: $it" напечатается только ОДИН РАЗ, так как upstream-поток выполняется
        // один раз и его результат разделяется (shared) между всеми подписчиками.
        launch { hotFlow.collect() }
        launch { hotFlow.collect() }

        delay(500)
        coroutineContext.cancelChildren()
    }

    /**
     * Работа с состояниями с помощью StateFlow.
     * StateFlow — это специальная, упрощенная версия SharedFlow, предназначенная для хранения
     * состояния. Она всегда хранит только последнее (текущее) значение.
     */
    @Test
    fun state(): Unit = runBlocking {
        // Создаем StateFlow с начальным состоянием "state1".
        val mshState = MutableStateFlow("state1")

        // Как и с SharedFlow, создаем неизменяемую версию для подписчиков.
        val shState = mshState.asStateFlow()

        // Ссылка для изменения состояния.
        val collector: FlowCollector<String> = mshState

        // Подписчик №1 (на изменяемый поток).
        launch { mshState.collect { println("MUT $it") } }

        // Подписчик №2 (на неизменяемый поток).
        launch { shState.collect { println("IMMUT $it") } }

        // Эмиттер, который быстро меняет состояние.
        launch {
            (1..20).forEach {
                delay(20)
                collector.emit("zz: $it")
            }
        }

        delay(100) // Ждем немного, чтобы состояние успело измениться несколько раз.

        // Главная особенность StateFlow — синхронный доступ к текущему значению через .value.
        // Это не приостанавливающая операция, мы можем получить состояние в любой момент.
        println("FINAL STATE: ${shState.value}")

        coroutineContext.cancelChildren()
    }
}
