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
 * Этот класс демонстрирует работу с несколькими асинхронными источниками данных (детекторами).
 * Мы рассмотрим три типа реализаций этих источников:
 * 1. Coroutine-based: Идеальная, неблокирующая реализация.
 * 2. Blocking: Плохая реализация, которая блокирует поток выполнения.
 * 3. Callback-based: Часто встречающийся способ интеграции с существующими API.
 *
 * Цель — увидеть, как разные реализации влияют на общую производительность и поведение системы,
 * когда их данные объединяются в один общий поток.
 */

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class DetectorsTest {

    /**
     * Фабричная функция, создающая список из трех различных детекторов.
     * Все они используют один и тот же источник случайных чисел (seq), но обрабатывают его по-разному.
     */
    private fun detectors() : List<Detector> {
        val random = Random.Default
        // seq - это бесконечная синхронная последовательность случайных чисел.
        // Она служит сырым источником данных для всех наших детекторов.
        val seq = sequence {
            while (true) {
                yield(random.nextDouble())
            }
        }

        return listOf(
            // 1. CoroutineDetector: Быстрый, неблокирующий детектор.
            //    Вероятно, внутри он использует `delay()` для приостановки, а не блокировки.
            //    Периодичность: 500 мс.
            CoroutineDetector("coroutine", seq, 500L),

            // 2. BlockingDetector: Медленный, блокирующий детектор.
            //    Вероятно, внутри он использует `Thread.sleep()`, что "замораживает" поток.
            //    Это плохая практика в корутинах. Периодичность: 800 мс.
            BlockingDetector("blocking", seq, 800L),

            // 3. CallbackDetector: Детектор, основанный на колбэках.
            //    Часто используется для интеграции с Java-библиотеками.
            //    Вероятно, реализован с помощью `callbackFlow`. Периодичность: 2000 мс (самый медленный).
            CallbackDetector("callback", seq, 2_000L)
        )
    }

    /**
     * Тест 1: "Сырые" данные.
     * Мы просто объединяем потоки от всех детекторов в один и смотрим на результат.
     * Оператор `merge` будет выдавать значения из любого потока, как только они поступают.
     * Здесь мы увидим, как `BlockingDetector` влияет на общую производительность.
     */
    @Test
    fun rawDetectorsData(): Unit = runBlocking {
        // 1. Получаем список детекторов и для каждого вызываем `samples()`, чтобы получить Flow<Sample>.
        //    Результат: List<Flow<Sample>>.
        detectors()
            .map { it.samples() }
            // 2. `merge` объединяет несколько потоков в один.
            //    Он не ждет, пока один поток закончится, а выдает значения по мере их поступления из любого источника.
            .merge()
            // 3. `onEach` - это побочный эффект. Для каждого полученного значения мы просто печатаем его.
            .onEach { println(it) }
            // 4. `launchIn` - это терминальный оператор, который запускает сбор потока в отдельной корутине.
            //    Это аналог `launch { flow.collect { ... } }`.
            .launchIn(this)

        // Даем потокам поработать 2 секунды, чтобы увидеть результат.
        delay(2000)
        // Важно отменить дочерние корутины (запущенные launchIn), иначе тест не завершится,
        // так как потоки данных бесконечны.
        coroutineContext.cancelChildren()
    }

    /**
     * Тест 2: "Раз в секунду или последнее значение".
     * Это более сложный сценарий. Мы хотим получать данные от каждого детектора не чаще,
     * чем раз в секунду. Если за это время от детектора не пришло новых данных,
     * мы хотим выдать последнее известное значение (с обновленным таймстемпом).
     */
    @Test
    fun oncePerSecondOrLast(): Unit = runBlocking {
        val desiredPeriod = 1000L // Наша цель — 1 значение в секунду.

        detectors()
            .map {
                it.samples()
                    // `transformLatest` — это ключевой оператор.
                    // Он работает так: когда приходит новое значение из upstream (it.samples()),
                    // он ОТМЕНЯЕТ выполнение предыдущего блока (если тот еще не завершился)
                    // и начинает выполнять блок заново с новым значением.
                    .transformLatest { sample ->
                        // Сразу же выдаем новое значение, как только оно пришло.
                        emit(sample)
                        // Затем входим в бесконечный цикл.
                        while (true) {
                            // Ждем нужный период (1 секунда).
                            delay(desiredPeriod)
                            // И выдаем то же самое значение, но с новым таймстемпом.
                            // Это "поддерживает" значение актуальным, если upstream не генерирует новые данные.
                            emit(sample.copy(timestamp = Instant.now()))
                        }
                    }
                    // `sample` здесь выполняет роль финального "регулятора".
                    // Он пропускает только одно значение за указанный период (desiredPeriod).
                    // Если за это время пришло несколько значений, он пропустит только самое последнее.
                    // Это гарантирует, что результирующий поток не будет "завален" данными.
                    .sample(desiredPeriod)
            }
            // Объединяем обработанные потоки от всех детекторов в один.
            .merge()
            // Печатаем результат.
            .onEach { println(it) }
            // Запускаем сбор.
            .launchIn(this)

        // Даем поработать дольше, чтобы увидеть логику в действии.
        delay(5_000)
        // Отменяем корутины.
        coroutineContext.cancelChildren()
    }
}
