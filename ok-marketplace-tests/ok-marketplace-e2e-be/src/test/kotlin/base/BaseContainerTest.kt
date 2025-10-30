package ru.otus.otuskotlin.marketplace.e2e.be.base

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class BaseContainerTest(protected val compose: AbstractDockerCompose) {
    @BeforeAll
    fun initContainer() {
        compose.start()
    }
    @AfterAll
    fun destroyContainer() {
        compose.stop()
    }
}
