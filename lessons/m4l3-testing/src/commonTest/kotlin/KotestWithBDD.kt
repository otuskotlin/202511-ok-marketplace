import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class KotestWithBDD() : BehaviorSpec({
    Given("State A") {
        val state = "State A"
        println("state A ")
        When("Action A") {
            val condition = 2
            println("in action A")
            Then("State => A1") {
                println("becomes A1")
                condition shouldBe 2
            }
        }
        When("Action B") {
            println("in action B")
            Then("State => B1") {
                println("becomes B1")
            }
        }
    }
})
