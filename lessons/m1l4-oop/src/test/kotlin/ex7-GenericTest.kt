import kotlin.test.Test
import kotlin.test.assertEquals

class GenericTest {
    @Test
    fun invariant() {
        // val obj: ISome<Number> = IntSome(1) // Не работает!!!
        assertEquals(3, (IntSome(1) + IntSome(2)).value)
    }

    @Test
    fun covariant() {
        @Suppress("UNUSED_VARIABLE")
        val obj: IParse<Number> = CovariantCls() // IParse<Int> -- Работает!
        assertEquals(3, CovariantCls().parse("3"))
    }

    @Test
    fun contravariant() {
        @Suppress("UNUSED_VARIABLE")
        val obj: IToString<Int> = ContravariantCls() // IToString<Number> -- Работает!
        assertEquals("3", ContravariantCls().toStr(3))
    }

    private interface ISome<T : ISome<T>> {
        operator fun plus(other: T): T
    }

    private class IntSome(val value: Int) : ISome<IntSome> {
        override fun plus(other: IntSome): IntSome = IntSome(value + other.value)
    }

    private interface IParse<out T : Number> {
        fun parse(str: String): T
    }

    private class CovariantCls : IParse<Int> {
        override fun parse(str: String): Int = str.toInt()
    }

    private interface IToString<in T> {
        fun toStr(i: T): String
    }

    private class ContravariantCls : IToString<Number> {
        override fun toStr(i: Number): String = i.toString()
    }
}