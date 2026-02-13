package ru.otus.otuskotlin.marketplace.e2e.be.docker

import ru.otus.otuskotlin.marketplace.e2e.be.base.AbstractDockerCompose

object WiremockDockerCompose : AbstractDockerCompose(
    "app-wiremock", 8080, "docker-compose-wiremock.yml"
)
