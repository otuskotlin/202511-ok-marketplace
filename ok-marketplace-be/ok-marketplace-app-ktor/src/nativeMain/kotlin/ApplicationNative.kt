package ru.otus.otuskotlin.marketplace.app.ktor

import io.ktor.server.cio.*
import io.ktor.server.config.yaml.*
import io.ktor.server.engine.*

fun main() {

    val appEnv = applicationEnvironment {
        val conf = YamlConfigLoader().load("./application.yaml")
            ?: throw RuntimeException("Cannot read application.yaml")
        config = conf
    }

    embeddedServer(CIO, environment = appEnv) {
        // Конфигурация движка (connector) теперь тут
        module()
    }.start(true)
}
