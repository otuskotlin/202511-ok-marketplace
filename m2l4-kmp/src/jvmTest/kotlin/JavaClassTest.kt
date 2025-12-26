import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class JavaClassTest {
    /**
     * Обращение к Java-классу
     * Использование аннотаций Null-safety в Java
     */
    @Test
    fun javaClassTest() {
        val jc = JavaClassExample("MyVal")
        // Обращение к setValue в Java
        jc.value = "MyVal1"
        // Обращение к getValue в Java
        assertEquals("MyVal1", jc.value)

        // Попробуйте раскомментировать. Здесь будет ошибка
//        val jcNull = JavaClassExample(null)
    }

    /**
     * Nullability
     */
    @Test
    fun javaClassNullableTest() {
        val jc = JavaClassExampleNull("MyVal")
        jc.value = "MyVal1"
        assertEquals("MyVal1", jc.value)

        // Здесь корректно Null
        val jcNull = JavaClassExampleNull(null)
        assertNull(jcNull.value)
    }

}
