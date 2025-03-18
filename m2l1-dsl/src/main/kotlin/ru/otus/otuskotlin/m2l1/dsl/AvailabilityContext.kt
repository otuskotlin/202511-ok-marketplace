package ru.otus.otuskotlin.m2l1.dsl

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.TemporalAdjusters

@UserDsl
class AvailabilityContext {

    var availability: MutableList<LocalDateTime> = mutableListOf()

    fun dayTimeOfWeek(day: DayOfWeek, time: String) {
        val d = LocalDate.now().with(TemporalAdjusters.next(day))
        val t = LocalTime.parse(time)
        availability.add(LocalDateTime.of(d, t))
    }
}
