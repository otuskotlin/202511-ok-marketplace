import kotlin.test.Test

class InliningTest {
    @Test
    fun `non local return demo`() {
        (0 until 20).forEach { index ->
            if (index == 4) return // Что будет напечатано?
            print(index)
        }
    }

    @Test
    fun `request token`() {
        requestToken(hasToken = false, onRefresh = {
            (0..20).forEach { num ->
                if (num == 4) return@forEach
            }
        }, onGenerate = {})
    }
}


inline fun requestToken(
    hasToken: Boolean,
    crossinline onRefresh: () -> Unit,
    noinline onGenerate: () -> Unit
) {
    if (hasToken) {
        httpCall("get-token", onGenerate)
    } else {
        httpCall("refresh-token") {
            onRefresh()
            onGenerate()
        }
    }
}

fun httpCall(url: String, callback: () -> Unit) {
    // noop
}