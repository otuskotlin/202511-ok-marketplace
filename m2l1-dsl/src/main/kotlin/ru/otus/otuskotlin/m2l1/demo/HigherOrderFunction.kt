package ru.otus.otuskotlin.m2l1.demo

fun calculate(operation: (Int, Int) -> Int, a: Int, b: Int): Int {
    return operation(a, b)
}

fun main() {
    val result = calculate({ x, y -> x + y }, 2, 3)
    print(result)
}
