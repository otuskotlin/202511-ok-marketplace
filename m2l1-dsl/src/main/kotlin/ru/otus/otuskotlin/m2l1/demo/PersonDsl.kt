package ru.otus.otuskotlin.m2l1.demo

@DslMarker
annotation class PersonDsl

@PersonDsl
data class Person(
    var id: Long = 0L,
    var name: String = "",
    var age: Int = 0,

    var contacts: Contacts = Contacts(),
)

@PersonDsl
data class Contacts(
    var phone: String = "",
    var email: String = "",
)

@PersonDsl
fun person(block: Person.() -> Unit): Person {
    return Person().apply(block)
}

@PersonDsl
fun Person.contacts(block: Contacts.() -> Unit) {
    this.contacts.apply(block)
}

fun main() {

    val p = person {
        id = 1L
        name = "John"
        age = 25
        contacts {
            email = "john@example.com"
            phone = "1234"
        }
    }

    println(p)
}
