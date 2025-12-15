package ru.otus.otuskotlin.m2l1.demo

import kotlin.test.Test

class HigherOrderFunction {
    fun calculate(operation: (Int, Int) -> Int, a: Int, b: Int): Int {
        return operation(a, b)
    }

    @Test
    fun `higher order function example`() {
        val result = calculate({ x, y -> x + y }, 2, 3)
        print(result)
    }
}
