package ru.otus.otuskotlin.marketplace.app.ktor.plugins

import io.ktor.server.application.*
import ru.otus.otuskotlin.marketplace.app.ktor.configs.ConfigPaths
import ru.otus.otuskotlin.marketplace.app.ktor.configs.PostgresConfig
//import ru.otus.otuskotlin.marketplace.backend.repo.postgresql.RepoAdSql
//import ru.otus.otuskotlin.marketplace.backend.repo.postgresql.SqlProperties
import ru.otus.otuskotlin.marketplace.common.repo.IRepoAd
import ru.otus.otuskotlin.marketplace.repo.inmemory.AdRepoInMemory

actual fun Application.getDatabaseConf(type: AdDbType): IRepoAd {
    val dbSettingPath = "${ConfigPaths.repository}.${type.confName}"
    return when (val dbSetting = environment.config.propertyOrNull(dbSettingPath)?.getString()?.lowercase()) {
        "in-memory", "inmemory", "memory", "mem" -> initInMemory()
        "postgres", "postgresql", "pg", "sql", "psql" -> initPostgres()
        else -> throw IllegalArgumentException(
            "$dbSettingPath has value of '$dbSetting', but it must be set in application.yml to one of: " +
                    "'inmemory', 'postgres'"
        )
    }
}

fun Application.initPostgres(): IRepoAd {
    val config = PostgresConfig(environment.config)
    return AdRepoInMemory()

}
