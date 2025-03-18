package ru.otus.otuskotlin.m2l1.dsl

import java.util.UUID

class UserBuilder {

    private var id: String = UUID.randomUUID().toString()

    private var nameContext: NameContext = NameContext()
    private var contactContext: ContactsContext = ContactsContext()
    private var actionContext: ActionsContext = ActionsContext()
    private var availabilityContext: AvailabilityContext = AvailabilityContext()

    @UserDsl
    fun name(block: NameContext.() -> Unit) {
        nameContext.apply(block)
    }

    @UserDsl
    fun contacts(block: ContactsContext.() -> Unit) {
        contactContext.apply(block)
    }

    @UserDsl
    fun actions(block: ActionsContext.() -> Unit) {
        actionContext.apply(block)
    }

    @UserDsl
    fun availability(block: AvailabilityContext.() -> Unit) {
        availabilityContext.apply(block)
    }

    fun build(): User {
        return User(
            id = id,
            firstName = nameContext.firstName,
            secondName = nameContext.secondName,
            lastName = nameContext.lastName,
            email = contactContext.email,
            phone = contactContext.phone,
            actions = actionContext.actions.toSet(),
            availability = availabilityContext.availability,
        )
    }
}
