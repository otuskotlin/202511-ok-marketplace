package ru.otus.otuskotlin.m2l1.demo

import kotlin.test.Test


class LambdaWithReceiver {
    data class Engineer(
        var name: String = "", var age: Int = 0, var country: String = ""
    )

    @Test
    fun `lambda with receiver example`() {
        val engineer = Engineer()
        engineer.name = "Name"
        engineer.age = 18
        engineer.country = "Country"

        val engineer2 = Engineer().apply {
            name = "Name2"
            age = 21
            country = "Country2"
        }

        println(engineer)
        println(engineer2)
    }
}

