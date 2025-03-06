import org.junit.jupiter.api.Test
import kotlin.test.assertIs

class CastTest {

    @Test
    fun cast() {
        val a = 1
        val b = a as Int
        assertIs<Int>(b)
    }

    @Test
    fun safeCast() {
        val x: String = "abc"
        val y = x as? String // y: String?
        assertIs<String?>(y)
    }

    @Test
    fun checks() {
        val a: Any = "abc"

        if (a is String) {
            assertIs<String>(a)
        }

        when (a) {
            is Int -> assertIs<Int>(a)
            !is Int -> assertIs<String>(a)
        }
    }

    @Test
    fun funcCast() {
        val nullable = null
        val whatTypeAmI = nullable?.toDouble() // Double?

        val str = "abc"
//        val whatTypeAmI2 = str.toDouble() Exception
        val whatTypeAmI2 = str.toDoubleOrNull()

        val strNulluble: String? = "12.34"
        val whatTypeAmI3 = strNulluble?.toDoubleOrNull()
    }
}
