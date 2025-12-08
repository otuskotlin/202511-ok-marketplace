import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class GenericTest {

    @Test
    fun elementAsListTest() {
        assertContains(elementAsList(12), 12)
        assertContains(elementAsList(48.0), 48.0)
        assertContains(elementAsList("A"), "A")
    }

    @Test
    fun genericTest() {
        assertEquals("String", variant2<String>())
    }

    @Test
    fun boundedGeneric() {
        assertEquals(9, NumberHandler<Int> { it * it }.handle(3))
        assertEquals(81.0, NumberHandler<Double> { it * it }.handle(9.0))
//        assertEquals("AA", NumberHandler<String> { it + it }.handle("A"))
    }


//    fun <T> willNotCompile(variable: T) {
//        println(T::class.java)
//    }


    private fun variant1(klass: KClass<*>): String = klass.simpleName ?: "(unknown)"
    private inline fun <reified T> variant2() = variant1(T::class)

    private fun <T> elementAsList(el: T): List<T> = listOf(el)

}

// variance demo
//class Box<T> where T : Any, T : Comparable<T>
class Box<T>

// bounded generics
fun interface NumberHandler<T : Number> {
    fun handle(num: T): T
}

