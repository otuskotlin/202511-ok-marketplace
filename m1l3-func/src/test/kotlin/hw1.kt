import org.junit.jupiter.api.Disabled
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AnalyticsTest {

    // -------------------------
    // Categories
    // -------------------------
    private val electronics = Category("Electronics")
    private val books = Category("Books")
    private val groceries = Category("Groceries")
    private val clothing = Category("Clothing")
    private val toys = Category("Toys")

    // -------------------------
    // Products
    // -------------------------
    private val iphone = Product(electronics, 900, "iPhone")
    private val laptop = Product(electronics, 1200, "Laptop")
    private val airpods = Product(electronics, 150, "AirPods")
    private val gamingPc = Product(electronics, 1800, "Gaming PC")
    private val smartwatch = Product(electronics, 250, "Smartwatch")
    private val monitor = Product(electronics, 300, "Monitor")
    private val coffeeMachine = Product(electronics, 280, "Coffee Machine")
    private val blender = Product(electronics, 70, "Blender")

    private val dune = Product(books, 25, "Dune")
    private val hobbit = Product(books, 20, "The Hobbit")
    private val cleanCode = Product(books, 35, "Clean Code")

    private val milk = Product(groceries, 3, "Milk")
    private val apples = Product(groceries, 4, "Apples")
    private val salmon = Product(groceries, 15, "Salmon")
    private val coffeeBeans = Product(groceries, 12, "Coffee Beans")
    private val rice = Product(groceries, 5, "Rice")

    private val hoodie = Product(clothing, 50, "Hoodie")
    private val jacket = Product(clothing, 120, "Jacket")

    private val lego = Product(toys, 130, "Lego Star Wars")
    private val puzzle = Product(toys, 18, "Puzzle 1000pcs")

    // -------------------------
    // Clients
    // -------------------------
    private val alice = Client(1, "Alice", listOf(iphone, dune, monitor))
    private val bob = Client(2, "Bob", listOf(laptop, milk, salmon, coffeeBeans))
    private val charlie = Client(3, "Charlie", listOf(coffeeMachine, blender))
    private val diana = Client(4, "Diana", listOf(jacket, lego))
    private val ethan = Client(5, "Ethan", listOf(gamingPc, smartwatch, airpods))

    private val fiona = Client(6, "Fiona", listOf(rice, apples))
    private val george = Client(7, "George", listOf(hoodie))
    private val helen = Client(8, "Helen", listOf(hobbit, cleanCode))
    private val ivan = Client(9, "Ivan", listOf(puzzle))

    private val jake = Client(10, "Jake", emptyList())
    private val karen = Client(11, "Karen", emptyList())

    private val allClients = listOf(
        alice, bob, charlie, diana, ethan,
        fiona, george, helen, ivan,
        jake, karen
    )

    private val analytics = Analytics(allClients)

    @Test
    @Disabled("Implement function first")
    fun `mostPopularCategory should return Electronics as it has the most products`() {
        assertEquals(electronics, analytics.mostPopularCategory())
    }

    @Test
    @Disabled("Implement function first")
    fun `mostPopularCategory should return null if all clients have empty product lists`() {
        val a = Analytics(listOf(jake, karen))
        assertNull(a.mostPopularCategory())
    }

    @Test
    @Disabled("Implement function first")
    fun `categoriesToTotalSumDesc should correctly sum and sort by total spent desc`() {
        val expected = linkedMapOf(
            electronics to (900 + 1200 + 150 + 1800 + 250 + 300 + 280 + 70),  // = 4950
            groceries to (3 + 4 + 15 + 12 + 5),                               // = 39
            books to (25 + 20 + 35),                                          // = 80
            clothing to (120 + 50),                                           // = 170
            toys to (130 + 18)                                                // = 148
        ).entries.sortedByDescending { it.value }.associate { it.toPair() }

        assertEquals(expected, analytics.categoriesToTotalSumDesc())
    }

    @Test
    @Disabled("Implement function first")
    fun `categoriesToTotalSumDesc should return empty map when no purchases exist`() {
        val a = Analytics(listOf(jake))
        assertEquals(emptyMap(), a.categoriesToTotalSumDesc())
    }

    @Test
    @Disabled("Implement function first")
    fun `clientsToSpentSumDesc should sort clients by total spending desc`() {
        val expected = listOf(
            ethan to (1800 + 250 + 150),          // 2200
            bob to (1200 + 3 + 15 + 12),          // 1230
            alice to (900 + 25 + 300),            // 1225
            charlie to (280 + 70),                // 350
            diana to (120 + 130),                 // 250
            helen to (20 + 35),                   // 55
            george to 50,                          // 50
            ivan to 18,                            // 18
            fiona to (5 + 4),                     // 9
            jake to 0,                             // 0
            karen to 0                             // 0
        )

        assertEquals(expected, analytics.clientsToSpentSumDesc().map { it.key to it.value })
    }

    @Test
    @Disabled("Implement function first")
    fun `clientsToSpentSumDesc should show zero for clients without purchases`() {
        val a = Analytics(listOf(jake))
        assertEquals(linkedMapOf(jake to 0), a.clientsToSpentSumDesc())
    }

    @Test
    @Disabled("Implement function first")
    fun `mostActiveClientsDesc should return clients sorted by product count desc`() {
        val expected = linkedMapOf(
            bob to 4,
            ethan to 3,
            alice to 3,
            diana to 2,
            charlie to 2,
            fiona to 2,
            helen to 2,
            george to 1,
            ivan to 1,
            jake to 0,
            karen to 0
        )

        assertEquals(expected, analytics.mostActiveClientsDesc())
    }

    @Test
    @Disabled("Implement function first")
    fun `mostActiveClientsDesc should return zero for empty clients`() {
        val a = Analytics(listOf(jake, karen))
        val expected = linkedMapOf(
            jake to 0,
            karen to 0
        )
        assertEquals(expected, a.mostActiveClientsDesc())
    }
}


class Analytics(val clients: List<Client>) {
    // Самая популярная категория (та, в которой КОЛИЧЕСТВО купленных товаров максимально)
    fun mostPopularCategory(): Category? = TODO()

    // Получить маппинг типа "Категория - сумма купленных товаров в ней" в порядке убывания
    // Если товаров в категории нет - вывести 0 в значении
    fun categoriesToTotalSumDesc(): Map<Category, Int> = TODO()

    // Получить маппинг типа "Клиент - сумма купленных товаров во всех категориях в порядке убывания
    // Если товаров не куплено - вывести 0 в значении
    fun clientsToSpentSumDesc(): Map<Client, Int> = TODO()

    // Вывести топ пользователей по КОЛИЧЕСТВУ купленных товаров в порядке убывания
    fun mostActiveClientsDesc(): Map<Client, Int> = TODO()

}

data class Client(val id: Int, val name: String, val products: List<Product>)
data class Product(val category: Category, val price: Int, val name: String)
data class Category(val name: String)