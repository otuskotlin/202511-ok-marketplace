package ru.otus.otuskotlin.m2l1

import ru.otus.otuskotlin.m2l1.dsl.Action.*
import ru.otus.otuskotlin.m2l1.dsl.buildUser
import ru.otus.otuskotlin.m2l1.dsl.fri
import ru.otus.otuskotlin.m2l1.dsl.tue
import ru.otus.otuskotlin.m2l1.dsl.wed

fun main() {

    // Итоговая последовательность разработки DSL
    // 1) создаем аннотацию @UserDsl
    // 2) создаем вспомогательные классы NameContext, ContactsContext, ActionsContext, AvailabilityContext
    // 3) добавляем расширение для AvailabilityContextExtensions
    // 4) создать основной класс UserBuilder
    // 5) добавить функцию buildUser
    // 6) пример использования DSL

    val user = buildUser {
        name {
            firstName = "John"
            secondName = "Wickovich"
            lastName = "Doe"
        }
        contacts {
            email = "john@mail.com"
            phone = "12345678"
        }
        actions {
            +READ
            +CREATE
            +ADD
        }
        availability {
            tue("12:00")
            wed("12:30")
            fri("15:00")
        }
    }

    println(user)
}
