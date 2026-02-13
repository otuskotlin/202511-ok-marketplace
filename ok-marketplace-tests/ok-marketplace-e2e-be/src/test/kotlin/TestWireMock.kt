package ru.otus.otuskotlin.marketplace.e2e.be

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import ru.otus.otuskotlin.marketplace.api.v1.models.AdDebug as AdDebugV1
import ru.otus.otuskotlin.marketplace.api.v2.models.AdDebug as AdDebugV2
import ru.otus.otuskotlin.marketplace.api.v1.models.AdRequestDebugMode as AdRequestDebugModeV1
import ru.otus.otuskotlin.marketplace.api.v2.models.AdRequestDebugMode as AdRequestDebugModeV2
import ru.otus.otuskotlin.marketplace.e2e.be.base.BaseContainerTest
import ru.otus.otuskotlin.marketplace.e2e.be.base.client.Client
import ru.otus.otuskotlin.marketplace.e2e.be.docker.WiremockDockerCompose
import ru.otus.otuskotlin.marketplace.e2e.be.base.client.RestClient
import ru.otus.otuskotlin.marketplace.e2e.be.scenarios.v1.ScenariosV1
import ru.otus.otuskotlin.marketplace.e2e.be.scenarios.v2.ScenariosV2

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestWireMock: BaseContainerTest(WiremockDockerCompose) {
    private val client: Client = RestClient(compose)
    @Test
    fun info() {
        println("${this::class.simpleName}")
    }

    @Nested
    internal inner class V1: ScenariosV1(client, AdDebugV1(mode = AdRequestDebugModeV1.PROD))
    @Nested
    internal inner class V2: ScenariosV2(client, AdDebugV2(mode = AdRequestDebugModeV2.PROD))

}