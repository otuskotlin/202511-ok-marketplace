package ru.otus.otuskotlin.m2l1.demo

class Greeter {
    var greeting: String = "Привет"

    fun greet() {
        println(greeting)
    }
}

fun main() {

    val greeter = Greeter()

    val action: Greeter.() -> Unit = {
        println(greeting.uppercase())
    }

    greeter.greet()
    greeter.action()
}
