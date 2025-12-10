import kotlin.test.Test
import kotlin.test.assertEquals

internal class ClassDelegationTest {
    @Test
    fun delegate() {
        val base = MyClass()
        val delegate = MyDelegate(base)

        println("Calling base")
        assertEquals("x", base.x())
        assertEquals("y", base.y())
        println("Calling delegate")
        assertEquals("delegate for (x)", delegate.x())
        assertEquals("y", delegate.y())
    }

    interface IDelegate {
        fun x(): String
        fun y(): String
    }

    class MyClass() : IDelegate {
        override fun x(): String {
            println("MyClass.x()")
            return "x"
        }

        override fun y(): String {
            println("MyClass.x()")
            return "y"
        }

    }

    class MyDelegate(
        private val del: IDelegate
    ) : IDelegate by del {
        override fun x(): String {
            println("Calling x")
            val str = del.x()
            println("Calling x done")
            return "delegate for ($str)"
        }
    }


    interface X {
        fun x(): String
    }

    class XImpl : X {
        override fun x(): String {
            println("Calling x in XImpl")
            return "x"
        }
    }

    interface Y {
        fun y(): String
    }

    class YImpl : Y {
        override fun y(): String {
            println("Calling y() in YImpl")
            return "y"
        }
    }

    interface Z {
        fun z(): String
    }

    // так выглядит реализация класса XYZ с делегацией реализации интерфейса
    class XYZ(
        x: X,
        y: Y,
    ) : X by x, Y by y, Z {

        override fun z(): String = "z"
    }

    // так бы выглядела реализация класса XYZ без синтаксиса делегации реализации интерфейса
    class XYZWithoutDelegation(
        private val x: X,
        private val y: Y,
    ) : X, Y, Z {

        override fun x(): String = x.x()
        override fun y(): String = y.y()
        override fun z(): String = "z"
    }


    @Test
    fun xyz() {
        val x = XImpl()
        val y = YImpl()

        val xyz = XYZ(x, y)
        xyz.x()
    }
}
