import kotlin.test.Test

@JvmInline
value class ProductId(
    val value: String
) {
    init {
        require(value.contains("^\\d+:\\d+$".toRegex()))
    }

    fun getFirstPart() = value.split(":").first()
}

data class Product(
    val id: ProductId,
    val name: String,
)

class ValueClasTest() {

    @Test
    fun productTest() {
        val id = ProductId("123:14")
        id.getFirstPart()

        val product = Product(id, "Product")
    }
}
