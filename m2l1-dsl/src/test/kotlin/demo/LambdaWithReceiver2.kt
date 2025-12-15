package ru.otus.otuskotlin.m2l1.demo

import kotlin.test.Test

class Greeter {
    var greeting: String = "Привет"

    fun greet() {
        println(greeting)
    }
}

class LambdaWithReceiver2 {
    @Test
    fun `lambda with receiver example 2`() {

        val greeter = Greeter()

        val action: Greeter.() -> Unit = {
            println(greeting.uppercase())
        }

        greeter.greet()
        greeter.action()
    }
}


