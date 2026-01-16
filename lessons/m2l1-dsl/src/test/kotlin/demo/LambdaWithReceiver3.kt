package ru.otus.otuskotlin.m2l1.demo

import kotlin.test.Test

fun String.modify(modification: String.() -> String): String {
    return this.modification()
}

class LambdaWithReceiver3 {

    @Test
    fun `lambda with receiver example 3`() {
        val result = "Hello".modify { uppercase() }

        println(result)
    }
}
