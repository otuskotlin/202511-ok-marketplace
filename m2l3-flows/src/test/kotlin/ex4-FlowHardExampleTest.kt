package ru.otus.otuskotlin.flows

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.runBlocking
import java.time.Instant
import kotlin.random.Random
import kotlin.test.Test

/**
 * Повышенная сложность.
 * Изучите различные виды реализации получения данных: flow, блокирующий (flow + Thread.sleep), callback.
 * Посмотрите как отражается на производительности реализация.
 */
@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class DetectorsTest {
    private fun detectors() : List<Detector> {
        val random = Random.Default
        val seq = sequence {
            while (true) {
                yield(random.nextDouble())
            }
        }

        return listOf(
            CoroutineDetector("coroutine", seq, 500L),
            BlockingDetector("blocking", seq, 800L),
            CallbackDetector("callback", seq, 2_000L)
        )
    }

    @Test
    fun rawDetectorsData(): Unit = runBlocking {
        // сырые данные от датчиков
        detectors()
            .map { it.samples() }
            .merge()
            .onEach { println(it) }
            .launchIn(this)

        delay(2000)
        coroutineContext.cancelChildren()
    }

    @Test
    fun oncePerSecondOrLast(): Unit = runBlocking {
        // данные от датчиков раз в секунду от каждого (если нового нет, то последнее)
        val desiredPeriod = 1000L
        detectors()
            .map {
                it.samples()
                    .transformLatest { sample ->
                        //println("Start transformLatest for ${sample.serialNumber}")
                        emit(sample)
                        while (true) {
                            delay(desiredPeriod)
                            //println("Add old value to flow in transformLatest for = ${sample.serialNumber}")
                            emit(sample.copy(timestamp = Instant.now()))
                        }
                    }
                    .sample(desiredPeriod)
            }
            .merge()
            .onEach { println(it) }
            .launchIn(this)

        delay(5_000)
        coroutineContext.cancelChildren()
    }
}