package ru.otus.otuskotlin.marketplace.app.ktor

import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.config.yaml.*
import io.ktor.server.engine.*

fun main() {

    val conf = YamlConfigLoader().load("./application.yaml")
        ?: throw RuntimeException("Cannot read application.yaml")

    val appEnv = applicationEnvironment {
        config = conf
    }

    embeddedServer(
        factory = CIO,
        environment = appEnv,
        configure = {
            this.connectors.add(EngineConnectorBuilder().apply {
                host = conf.host
                port = conf.port
            })
        }
    ) {
        module()
    }.start(true)
}
