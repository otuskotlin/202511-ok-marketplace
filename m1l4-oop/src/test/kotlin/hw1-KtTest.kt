import kotlin.test.*

@Ignore
class Hw1KtTest {
    // task 1 - make a Rectangle class that will have width and height
    // as well as the area calculation method - area()
    // the test below should pass - uncomment the code in it
    @Test
    fun rectangleArea() {
        val r = Rectangle(10, 20)
        assertEquals(200, r.area())
        assertEquals(10, r.width)
        assertEquals(20, r.height)
    }

    // task 2 - make the Rectangle.toString() method
    // the test below should pass - uncomment the code in it
    @Test
    fun rectangleToString() {
        val r = Rectangle(10, 20)
        assertEquals("Rectangle(10x20)", r.toString())

    }

    // task 3 - make Rectangle.equals() and Rectangle.hashCode() methods
    // the test below should pass - uncomment the code in it
    @Test
    fun rectangleEquals() {
        val a = Rectangle(10, 20)
        val b = Rectangle(10, 20)
        val c = Rectangle(20, 10)
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
        assertFalse(a === b)
        assertNotEquals(a, c)

    }

    // task 4 - make the Square class
    // the test below should pass - uncomment the code in it
    @Test
    fun squareEquals() {
        val a = Square(10)
        val b = Square(10)
        val c = Square(20)
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
        assertFalse(a === b)
        assertNotEquals(a, c)
        println(a)

    }

    // task 5 - make the Figure interface with the area() method, inherit Rectangle and Square from it
    // the test below should pass - uncomment the code in it
    @Test
    fun figureArea() {
        var f: Figure = Rectangle(10, 20)
        assertEquals(f.area(), 200)

        f = Square(10)
        assertEquals(f.area(), 100)

    }

    // task 6 - make the diffArea(a, b) method
    // the test below should pass - uncomment the code in it
    @Test
    fun diffArea() {
        val a = Rectangle(10, 20)
        val b = Square(10)
        assertEquals(diffArea(a, b), 100)
    }

}

class Rectangle(
    val width: Int,
    val height: Int,
) : Figure {
    override fun area() = width * height

    override fun toString(): String {
        return "Rectangle(${width}x${height})"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Rectangle

        if (width != other.width) return false
        if (height != other.height) return false

        return true
    }

    override fun hashCode(): Int {
        var result = width
        result = 31 * result + height
        return result
    }


}

data class Square(
    val width: Int,
) : Figure {
    override fun area() = width * width
}

interface Figure {
    fun area(): Int
}


fun diffArea(figure1: Figure, figure2: Figure) = figure1.area() - figure2.area()
