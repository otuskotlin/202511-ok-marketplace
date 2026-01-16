import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class IteratorTest {
    private val list = listOf("string", "1", "2")

    @Test
    fun immutable() {
        val iter: Iterator<String> = list.iterator()
        // iter.remove() // Not allowed
        assertEquals("string", iter.next())
    }

    @Test
    fun mutable() {
        val mutableList = list.toMutableList()
        val mutableIterator: MutableIterator<String> = mutableList.iterator()
        mutableIterator.next()
        mutableIterator.remove()
        assertEquals("1", mutableIterator.next())
        assertFalse(mutableList.contains("string"))
    }
}