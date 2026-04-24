package ru.otus.otuskotlin.marketplace.e2e.be.docker

import ru.otus.otuskotlin.marketplace.e2e.be.base.AbstractDockerCompose


object KtorJvmKeycloakDockerCompose : AbstractDockerCompose(
    "envoy", 8080, "docker-compose-ktor-keycloak-jvm.yml"
)
