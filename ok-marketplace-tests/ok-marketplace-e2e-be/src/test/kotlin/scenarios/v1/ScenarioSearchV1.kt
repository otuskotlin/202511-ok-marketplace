package ru.otus.otuskotlin.marketplace.e2e.be.scenarios.v1

import io.kotest.engine.runBlocking
import org.junit.jupiter.api.Test
import ru.otus.otuskotlin.marketplace.api.v1.models.*
import ru.otus.otuskotlin.marketplace.e2e.be.base.client.Client
import ru.otus.otuskotlin.marketplace.e2e.be.scenarios.v1.base.sendAndReceive
import ru.otus.otuskotlin.marketplace.e2e.be.scenarios.v1.base.someCreateAd
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.fail

abstract class ScenarioSearchV1(
    private val client: Client,
    private val debug: AdDebug? = null
) {
    @Test
    fun search() = runBlocking {
        val objs = listOf(
            someCreateAd,
            someCreateAd.copy(title = "Selling Bolt"),
            someCreateAd.copy(title = "Selling Nut"),
        ).map { obj ->
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
            cObj
        }

        val sObj = AdSearchFilter(searchString = "Selling")
        val resSearch = client.sendAndReceive(
            "ad/search",
            AdSearchRequest(
                requestType = "search",
                debug = debug,
                adFilter = sObj,
            )
        ) as AdSearchResponse

        assertEquals(ResponseResult.SUCCESS, resSearch.result)

        val rsObj: List<AdResponseObject> = resSearch.ads ?: fail("No ads in Search response")
        val titles = rsObj.map { it.title }
        assertContains(titles, "Selling Bolt")
        assertContains(titles, "Selling Nut")

        objs.forEach { obj ->
            val resDelete = client.sendAndReceive(
                "ad/delete", AdDeleteRequest(
                    requestType = "delete",
                    debug = debug,
                    ad = AdDeleteObject(obj.id, obj.lock),
                )
            ) as AdDeleteResponse

            assertEquals(ResponseResult.SUCCESS, resDelete.result)
        }
    }
}