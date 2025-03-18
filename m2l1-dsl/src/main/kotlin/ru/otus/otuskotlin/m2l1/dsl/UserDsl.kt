package ru.otus.otuskotlin.m2l1.dsl

@DslMarker
annotation class UserDsl

@UserDsl
fun buildUser(block: UserBuilder.() -> Unit): User {
    return UserBuilder().apply(block).build()
}
