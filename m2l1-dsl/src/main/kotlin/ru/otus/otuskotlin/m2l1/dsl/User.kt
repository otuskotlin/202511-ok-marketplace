package ru.otus.otuskotlin.m2l1.dsl

import java.time.LocalDateTime
import java.util.UUID

data class User (
    val id: String = UUID.randomUUID().toString(),

    // NameContext
    val firstName: String,
    val secondName: String,
    val lastName: String,

    // ContactsContext
    val phone: String?,
    val email: String,

    // ActionsContext
    val actions: Set<Action>,

    // AvailabilityContext
    val availability: List<LocalDateTime>
)
