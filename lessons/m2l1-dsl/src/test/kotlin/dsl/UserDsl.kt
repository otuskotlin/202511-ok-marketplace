package dsl

@DslMarker
annotation class UserDsl

@UserDsl
fun buildUser(block: UserBuilder.() -> Unit): User {
    return UserBuilder().apply(block).build()
}
