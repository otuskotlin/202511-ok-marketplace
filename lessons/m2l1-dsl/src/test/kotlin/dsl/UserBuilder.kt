package dsl

import java.util.UUID

@UserDsl
class UserBuilder {

    private var id: String = UUID.randomUUID().toString()

    private var nameContext: NameContext = NameContext()
    private var contactContext: ContactsContext = ContactsContext()
    private var actionContext: ActionsContext = ActionsContext()
    private var availabilityContext: AvailabilityContext = AvailabilityContext()


    fun name(block: NameContext.() -> Unit) {
        nameContext.apply(block)
    }

    fun contacts(block: ContactsContext.() -> Unit) {
        contactContext.apply(block)
    }

    fun actions(block: ActionsContext.() -> Unit) {
        actionContext.apply(block)
    }

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
