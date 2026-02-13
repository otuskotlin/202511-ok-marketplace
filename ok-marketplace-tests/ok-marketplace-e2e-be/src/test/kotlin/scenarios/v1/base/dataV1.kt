package ru.otus.otuskotlin.marketplace.e2e.be.scenarios.v1.base

import ru.otus.otuskotlin.marketplace.api.v1.models.*

val someCreateAd = AdCreateObject(
    title = "Требуется болт",
    description = "Требуется болт 100x5 с шестигранной шляпкой",
    adType = DealSide.DEMAND,
    visibility = AdVisibility.PUBLIC
)
