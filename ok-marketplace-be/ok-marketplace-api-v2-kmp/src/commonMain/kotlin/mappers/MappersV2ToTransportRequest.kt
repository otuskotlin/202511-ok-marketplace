package ru.otus.otuskotlin.marketplace.api.v2.mappers

import ru.otus.otuskotlin.marketplace.api.v2.models.AdCreateObject
import ru.otus.otuskotlin.marketplace.api.v2.models.AdDeleteObject
import ru.otus.otuskotlin.marketplace.api.v2.models.AdReadObject
import ru.otus.otuskotlin.marketplace.api.v2.models.AdUpdateObject
import ru.otus.otuskotlin.marketplace.common.models.MkplAd
import ru.otus.otuskotlin.marketplace.common.models.MkplAdLock

fun MkplAd.toTransportCreateAd() = AdCreateObject(
    title = title,
    description = description,
    adType = adType.toTransportAd(),
    visibility = visibility.toTransportAd(),
    )

fun MkplAd.toTransportReadAd() = AdReadObject(
    id = id.toTransportAd()
)

fun MkplAd.toTransportUpdateAd() = AdUpdateObject(
    id = id.toTransportAd(),
    title = title,
    description = description,
    adType = adType.toTransportAd(),
    visibility = visibility.toTransportAd(),
    lock = lock.toTransportAd(),
)

internal fun MkplAdLock.toTransportAd() = takeIf { it != MkplAdLock.NONE }?.asString()

fun MkplAd.toTransportDeleteAd() = AdDeleteObject(
    id = id.toTransportAd(),
    lock = lock.toTransportAd(),
)