import org.junit.jupiter.api.Test
import kotlin.test.assertEquals


class IfWhenTest {

    @Test
    fun ifExpression() {
        val i = 1

        val a = if (i > 0) {
            "Positive"
        } else if (i == 0) {
            "Zero"
        } else {
            "Negative"
        }

        assertEquals("Positive", a)
    }

    @Test
    fun whenExpression() {
        val i = 0

        val a = when {
            i > 0 -> "Positive"
            i == 0 -> "Zero"
            else -> "Negative"
        }

        assertEquals("Zero", a)
    }

    @Test
    fun whenExpression2() {
        val i = 5

        val a = when (i) {
            0 -> "Zero"
            1 -> "One"
            else -> "Other"
        }

        assertEquals("Other", a)
    }
}
