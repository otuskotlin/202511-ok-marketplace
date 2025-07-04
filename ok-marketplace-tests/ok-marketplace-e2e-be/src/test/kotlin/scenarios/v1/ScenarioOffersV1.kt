package ru.otus.otuskotlin.marketplace.e2e.be.scenarios.v1

import io.kotest.engine.runBlocking
import org.junit.jupiter.api.Test
import ru.otus.otuskotlin.marketplace.api.v1.models.*
import ru.otus.otuskotlin.marketplace.e2e.be.base.client.Client
import ru.otus.otuskotlin.marketplace.e2e.be.scenarios.v1.base.sendAndReceive
import ru.otus.otuskotlin.marketplace.e2e.be.scenarios.v1.base.someCreateAd
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

abstract class ScenarioOffersV1(
    private val client: Client,
    private val debug: AdDebug? = null
) {
    @Test
    fun offers() = runBlocking {
        val objs = listOf(
            someCreateAd,
            someCreateAd.copy(title = "Some Bolt", adType = DealSide.SUPPLY),
            someCreateAd.copy(title = "Some Bolt", adType = DealSide.DEMAND),
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
        val (_, oSupply, oDemand) = objs

        val sObj = AdReadObject(id = oSupply.id)
        val resOffers = client.sendAndReceive(
            "ad/offers",
            AdOffersRequest(
                requestType = "offers",
                debug = debug,
                ad = sObj,
            )
        ) as AdOffersResponse

        assertEquals(ResponseResult.SUCCESS, resOffers.result)

        val rsObj: List<AdResponseObject> = resOffers.ads ?: fail("No ads in Search response")
        println(rsObj)
        assertTrue { rsObj.map { it.adType }.all { it == oDemand.adType } }
        val titles = rsObj.map { it.title }
        assertContains(titles, "Some Bolt")

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