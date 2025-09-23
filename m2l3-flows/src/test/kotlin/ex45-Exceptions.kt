package ru.otus.otuskotlin.flows

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.test.Test
import java.time.Instant
import kotlin.random.Random

class ExceptionsTest {
    private val flow = flow {
        delay(500)
        emit("1")
        delay(500)
        emit("2")

        val a = 1 / 0

        delay(500)
        emit("3")
        delay(500)
        emit("4")
    }

    @Test
    fun exception() = runBlocking {
        try {
            flow.collect { println(it) }
        } catch (e: Exception) {
            println("Caught $e")
        }
    }

    @Test
    fun catchOperator() = runBlocking {
        flow
            .catch { e -> println("Caught $e") }
            .collect { println(it) }
    }

    @Test
    fun retryOperator() = runBlocking {
        flow
            .retry(2)
            .catch { e -> println("Caught $e") }
            .collect { println(it) }
    }



}