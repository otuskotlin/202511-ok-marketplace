package dsl

@UserDsl
class ActionsContext {

    var actions: MutableList<Action> = mutableListOf()

    fun add(action: Action) = actions.add(action)

    fun add(actionString: String) = add(Action.valueOf(actionString))

    operator fun Action.unaryPlus() = add(this)

    operator fun String.unaryPlus() = add(this)
}
