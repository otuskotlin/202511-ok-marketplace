package ru.otus.otuskotlin.marketplace.e2e.be.scenarios.v1

import io.kotest.engine.runBlocking
import org.junit.jupiter.api.Test
import ru.otus.otuskotlin.marketplace.api.v1.models.*
import ru.otus.otuskotlin.marketplace.e2e.be.base.client.Client
import ru.otus.otuskotlin.marketplace.e2e.be.scenarios.v1.base.sendAndReceive
import ru.otus.otuskotlin.marketplace.e2e.be.scenarios.v1.base.someCreateAd
import kotlin.test.assertEquals
import kotlin.test.fail

abstract class ScenarioCreateDeleteV1(
    private val client: Client,
    private val debug: AdDebug? = null
) {
    @Test
    fun createDelete() = runBlocking {
        val obj = someCreateAd
        val resCreate = client.sendAndReceive(
            "ad/create", AdCreateRequest(
                requestType = "create",
                debug = debug,
                ad = obj,
            )
        ) as AdCreateResponse

        assertEquals(ResponseResult.SUCCESS, resCreate.result)

        val cObj: AdResponseObject = resCreate.ad ?: fail("No ad in Create response")
        assertEquals(obj.title, cObj.title)
        assertEquals(obj.description, cObj.description)
        assertEquals(obj.visibility, cObj.visibility)
        assertEquals(obj.adType, cObj.adType)

        val resDelete = client.sendAndReceive(
            "ad/delete", AdDeleteRequest(
                requestType = "delete",
                debug = debug,
                ad = AdDeleteObject(cObj.id, cObj.lock),
            )
        ) as AdDeleteResponse

        assertEquals(ResponseResult.SUCCESS, resDelete.result)

        val dObj: AdResponseObject = resDelete.ad ?: fail("No ad in Delete response")
        assertEquals(obj.title, dObj.title)
        assertEquals(obj.description, dObj.description)
        assertEquals(obj.visibility, dObj.visibility)
        assertEquals(obj.adType, dObj.adType)
    }
}