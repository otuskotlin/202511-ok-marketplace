package ru.otus.otuskotlin.marketplace.e2e.be.scenarios.v1

import io.kotest.engine.runBlocking
import org.junit.jupiter.api.Test
import ru.otus.otuskotlin.marketplace.api.v1.models.*
import ru.otus.otuskotlin.marketplace.e2e.be.base.client.Client
import ru.otus.otuskotlin.marketplace.e2e.be.scenarios.v1.base.sendAndReceive
import ru.otus.otuskotlin.marketplace.e2e.be.scenarios.v1.base.someCreateAd
import kotlin.test.assertEquals
import kotlin.test.fail

abstract class ScenarioUpdateV1(
    private val client: Client,
    private val debug: AdDebug? = null
) {
    @Test
    fun update() = runBlocking {
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

        val uObj = AdUpdateObject(
            id = cObj.id,
            lock = cObj.lock,
            title = "Selling Nut",
            description = cObj.description,
            adType = cObj.adType,
            visibility = cObj.visibility,
        )
        val resUpdate = client.sendAndReceive(
            "ad/update",
            AdUpdateRequest(
                requestType = "update",
                debug = debug,
                ad = uObj,
            )
        ) as AdUpdateResponse

        assertEquals(ResponseResult.SUCCESS, resUpdate.result)

        val ruObj: AdResponseObject = resUpdate.ad ?: fail("No ad in Update response")
        assertEquals(uObj.title, ruObj.title)
        assertEquals(uObj.description, ruObj.description)
        assertEquals(uObj.visibility, ruObj.visibility)
        assertEquals(uObj.adType, ruObj.adType)

        val resDelete = client.sendAndReceive(
            "ad/delete", AdDeleteRequest(
                requestType = "delete",
                debug = debug,
                ad = AdDeleteObject(cObj.id, resUpdate.ad?.lock),
            )
        ) as AdDeleteResponse

        assertEquals(ResponseResult.SUCCESS, resDelete.result)
    }
}