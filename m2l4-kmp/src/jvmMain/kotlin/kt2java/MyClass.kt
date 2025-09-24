package ru.otus.otuskotlin.kmp.kt2java

import java.io.Serial
import java.io.SyncFailedException

class MyClass @JvmOverloads constructor(
    // прямой доступ к полю без геттеров и сеттеров
    @JvmField
    val a: String = "a-prop",

    // аннотация на геттер
    @get:Serial
    // аннотация на поле
    @field:Serial
    // сеттера нет
    val b: String = "b-prop",

    // аннотации на геттер и сеттер, поле недоступно
    @get:Serial
    @set:Serial
    var c: String = "c-prop",

    ) {

    @Synchronized // Это аналог synchronized in Java
    @Throws(SyncFailedException::class) // Здесь объявляем checked exception
    fun syncFun() {
        val x = ""
        synchronized(x) {

        }
        println("synchronized method")
    }

    private val lock = ""
    @Throws(SyncFailedException::class) // Здесь объявляем checked exception
    fun funWithSync() {
        synchronized(lock) {
            println("synchronized method")
        }
    }

}
