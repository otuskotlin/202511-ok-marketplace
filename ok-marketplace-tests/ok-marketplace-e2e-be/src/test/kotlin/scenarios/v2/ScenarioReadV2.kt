package ru.otus.otuskotlin.marketplace.e2e.be.scenarios.v2

import io.kotest.engine.runBlocking
import org.junit.jupiter.api.Test
import ru.otus.otuskotlin.marketplace.api.v2.models.*
import ru.otus.otuskotlin.marketplace.e2e.be.base.client.Client
import ru.otus.otuskotlin.marketplace.e2e.be.scenarios.v2.base.sendAndReceive
import ru.otus.otuskotlin.marketplace.e2e.be.scenarios.v2.base.someCreateAd
import kotlin.test.assertEquals
import kotlin.test.fail

abstract class ScenarioReadV2(
    private val client: Client,
    private val debug: AdDebug? = null
) {
    @Test
    fun read() = runBlocking {
        val obj = someCreateAd
        val resCreate = client.sendAndReceive<AdCreateRequest,AdCreateResponse>(
            "ad/create", AdCreateRequest(
                debug = debug,
                ad = obj,
            )
        )

        assertEquals(ResponseResult.SUCCESS, resCreate.result)

        val cObj: AdResponseObject = resCreate.ad ?: fail("No ad in Create response")
        assertEquals(obj.title, cObj.title)
        assertEquals(obj.description, cObj.description)
        assertEquals(obj.visibility, cObj.visibility)
        assertEquals(obj.adType, cObj.adType)

        val rObj = AdReadObject(
            id = cObj.id,
        )
        val resRead = client.sendAndReceive<AdReadRequest,AdReadResponse>(
            "ad/read",
            AdReadRequest(
                debug = debug,
                ad = rObj,
            )
        )

        assertEquals(ResponseResult.SUCCESS, resRead.result)

        val rrObj: AdResponseObject = resRead.ad ?: fail("No ad in Read response")
        assertEquals(obj.title, rrObj.title)
        assertEquals(obj.description, rrObj.description)
        assertEquals(obj.visibility, rrObj.visibility)
        assertEquals(obj.adType, rrObj.adType)

        val resDelete = client.sendAndReceive<AdDeleteRequest,AdDeleteResponse>(
            "ad/delete", AdDeleteRequest(
                debug = debug,
                ad = AdDeleteObject(cObj.id, cObj.lock),
            )
        )

        assertEquals(ResponseResult.SUCCESS, resDelete.result)

        val dObj: AdResponseObject = resDelete.ad ?: fail("No ad in Delete response")
        assertEquals(obj.title, dObj.title)
        assertEquals(obj.description, dObj.description)
        assertEquals(obj.visibility, dObj.visibility)
        assertEquals(obj.adType, dObj.adType)
    }
}