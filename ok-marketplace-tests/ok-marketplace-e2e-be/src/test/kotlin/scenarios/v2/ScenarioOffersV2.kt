package ru.otus.otuskotlin.marketplace.e2e.be.scenarios.v2

import io.kotest.engine.runBlocking
import org.junit.jupiter.api.Test
import ru.otus.otuskotlin.marketplace.api.v2.models.*
import ru.otus.otuskotlin.marketplace.e2e.be.base.client.Client
import ru.otus.otuskotlin.marketplace.e2e.be.scenarios.v2.base.sendAndReceive
import ru.otus.otuskotlin.marketplace.e2e.be.scenarios.v2.base.someCreateAd
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

abstract class ScenarioOffersV2(
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
            val resCreate = client.sendAndReceive<AdCreateRequest, AdCreateResponse>(
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
            cObj
        }
        val (_, oSupply, oDemand) = objs

        val sObj = AdReadObject(id = oSupply.id)
        val resOffers = client.sendAndReceive<AdOffersRequest, AdOffersResponse>(
            "ad/offers",
            AdOffersRequest(
                debug = debug,
                ad = sObj,
            )
        )

        assertEquals(ResponseResult.SUCCESS, resOffers.result)

        val rsObj: List<AdResponseObject> = resOffers.ads ?: fail("No ads in Search response")
        println(rsObj)
        assertTrue { rsObj.map { it.adType }.all { it == oDemand.adType } }
        val titles = rsObj.map { it.title }
        assertContains(titles, "Some Bolt")

        objs.forEach { obj ->
            val resDelete = client.sendAndReceive<AdDeleteRequest, AdDeleteResponse>(
                "ad/delete", AdDeleteRequest(
                    debug = debug,
                    ad = AdDeleteObject(obj.id, obj.lock),
                )
            )

            assertEquals(ResponseResult.SUCCESS, resDelete.result)
        }
    }
}