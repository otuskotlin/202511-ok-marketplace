package ru.otus.otuskotlin.coroutines

import kotlinx.coroutines.*
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.CoroutineContext
import kotlin.test.Test

/**
 * Класс для демонстрации работы с CoroutineScope и CoroutineContext.
 * CoroutineScope — это "контейнер" или "область видимости" для корутин.
 * Он определяет жизненный цикл корутин, запущенных внутри него.
 * CoroutineContext — это набор элементов (диспетчер, задача, имя и т.д.), которые определяют,
 * *как* будет выполняться корутина.
 */
class Ex6Scope {

    /**
     * Тест показывает создание самого простого CoroutineScope.
     * Для создания скоупа достаточно передать ему любой элемент CoroutineContext.
     */
    @Test
    fun create1() {
        // Создаем скоуп, передавая ему только один элемент контекста — диспетчер.
        // Dispatchers.Main указывает, что корутины в этом скоупе должны выполняться в главном потоке
        // (например, потоке UI в Android).
        // Недостающие элементы контекста (такие как Job) будут добавлены по умолчанию при запуске корутины.
        val scope = CoroutineScope(Dispatchers.Main)

        println("scope: $scope")
        // Вывод покажет, что у скоупа есть контекст, содержащий Dispatchers.Main.
    }

    /**
     * Тест демонстрирует объединение нескольких элементов CoroutineContext.
     * Элементы контекста можно объединять с помощью оператора '+'.
     */
    @Test
    fun create2() {
        // Создаем скоуп с более сложным контекстом, состоящим из трех элементов:
        // 1. Dispatchers.Main: определяет поток выполнения.
        // 2. Job(): создает новую "задачу", которая будет родительской для всех корутин в этом скоупе.
        //    Это позволяет управлять жизненным циклом всех корутин сразу (например, отменить их всех).
        // 3. CoroutineName("create2"): задает имя для корутин в этом скоупе, что удобно для отладки.
        val scope = CoroutineScope(Dispatchers.Main + Job() + CoroutineName("create2"))

        println("scope: $scope")
        // Вывод покажет объединенный контекст со всеми тремя элементами.
    }

    /**
     * Тест показывает, как создавать и использовать собственные (пользовательские) элементы контекста.
     */
    @Test
    fun create3() {
        // Создаем скоуп, используя наш собственный класс SomeData в качестве единственного элемента контекста.
        val scope = CoroutineScope(SomeData(10, 20))

        // Мы можем получить наш элемент из контекста скоупа, используя его ключ (SomeData).
        // Это типобезопасный способ доступа к данным.
        val data = scope.coroutineContext[SomeData]

        println("scope: $scope, $data")
        // Вывод покажет скоуп с контекстом, содержащим только наш SomeData, и затем выведет сами данные.
    }

    /**
     * Вспомогательная функция-расширение для CoroutineScope, чтобы красиво выводить содержимое контекста.
     */
    private fun CoroutineScope.scopeToString(): String =
    // Из coroutineContext мы можем получить любой элемент по его ключу.
    // Job — ключ для задачи.
    // ContinuationInterceptor — это базовый класс для всех диспетчеров (Dispatchers).
        // SomeData — наш собственный ключ.
        "Job = ${coroutineContext[Job]}, Dispatcher = ${coroutineContext[ContinuationInterceptor]}, Data = ${coroutineContext[SomeData]}"

    /**
     * Тест демонстрирует, как дочерние корутины наследуют и переопределяют контекст своего скоупа.
     */
    @Test
    fun defaults() {
        // Создаем скоуп с нашими данными по умолчанию.
        val scope = CoroutineScope(SomeData(10, 20))

        // Запускаем первую корутину. Она НАСЛЕДУЕТ контекст скоупа.
        scope.launch {
            println("Child1: ${scopeToString()}")
            // Вывод: Job = ..., Dispatcher = ..., Data = SomeData(x=10, y=20)
        }

        // Запускаем вторую корутину, но передаем ей ДРУГОЙ элемент SomeData.
        // Этот элемент ПЕРЕОПРЕДЕЛИТ (заменит) элемент, унаследованный от скоупа.
        scope.launch(SomeData(1, 2)) {
            println("Child2: ${scopeToString()}")
            // Вывод: Job = ..., Dispatcher = ..., Data = SomeData(x=1, y=2)
        }

        // Выводим контекст самого скоупа.
        println("This: ${scope.scopeToString()}")
        // Вывод: Job = null, Dispatcher = null, Data = SomeData(x=10, y=20)
        // (Job и Dispatcher равны null, потому что мы их не добавляли в сам скоуп,
        //  они будут добавлены по умолчанию при запуске корутины)

        Thread.sleep(100) // Ждем, чтобы асинхронные println успели выполниться
    }

    /**
     * Пример создания собственного элемента CoroutineContext.
     * Это позволяет передавать в корутины любые ваши данные (например, токен аутентификации,
     * ID пользователя, конфигурацию запроса) в типобезопасной манере.
     */
    data class SomeData(val x: Int, val y: Int) : AbstractCoroutineContextElement(SomeData) {
        // 1. Наследуемся от AbstractCoroutineContextElement, передавая ему ключ (SomeData).
        //    Это делает наш класс полноценным элементом контекста.

        // 2. Создаем companion object, который реализует CoroutineContext.Key<SomeData>.
        //    Этот объект служит уникальным ключом для нашего типа данных.
        //    Именно благодаря ему мы можем использовать синтаксис `context[SomeData]`.
        companion object : CoroutineContext.Key<SomeData>
    }
}
