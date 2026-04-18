package ru.otus.otuskotlin.marketplace.app.ktor.plugins

import io.ktor.server.application.*
import ru.otus.otuskotlin.marketplace.app.ktor.configs.CassandraConfig
import ru.otus.otuskotlin.marketplace.app.ktor.configs.ConfigPaths
import ru.otus.otuskotlin.marketplace.app.ktor.configs.GremlinConfig
import ru.otus.otuskotlin.marketplace.app.ktor.configs.PostgresConfig
import ru.otus.otuskotlin.marketplace.backend.repo.cassandra.RepoAdCassandra
import ru.otus.otuskotlin.marketplace.backend.repo.postgresql.RepoAdSql
import ru.otus.otuskotlin.marketplace.backend.repo.postgresql.SqlProperties
import ru.otus.otuskotlin.marketplace.backend.repository.gremlin.AdRepoGremlin
import ru.otus.otuskotlin.marketplace.common.repo.IRepoAd

actual fun Application.getDatabaseConf(type: AdDbType): IRepoAd {
    val dbSettingPath = "${ConfigPaths.repository}.${type.confName}"
    return when (val dbSetting = environment.config.propertyOrNull(dbSettingPath)?.getString()?.lowercase()) {
        "in-memory", "inmemory", "memory", "mem" -> initInMemory()
        "postgres", "postgresql", "pg", "sql", "psql" -> initPostgres()
        "cassandra", "nosql", "cass" -> initCassandra()
        "arcade", "arcadedb", "graphdb", "gremlin", "g", "a" -> initGremlin()
        else -> throw IllegalArgumentException(
            "$dbSettingPath has value of '$dbSetting', but it must be set in application.yml to one of: " +
                    "'inmemory', 'postgres', 'cassandra', 'gremlin'"
        )
    }
}

fun Application.initPostgres(): IRepoAd {
    val config = PostgresConfig(environment.config)
    return RepoAdSql(
        properties = SqlProperties(
            host = config.host,
            port = config.port,
            user = config.user,
            password = config.password,
            schema = config.schema,
            database = config.database,
        ),
    )
}

private fun Application.initCassandra(): IRepoAd {
    val config = CassandraConfig(environment.config)
    return RepoAdCassandra(
        keyspaceName = config.keyspace,
        host = config.host,
        port = config.port,
        user = config.user,
        pass = config.pass,
    )
}

private fun Application.initGremlin(): IRepoAd {
    val config = GremlinConfig(environment.config)
    return AdRepoGremlin(
        hosts = config.host,
        port = config.port,
        user = config.user,
        pass = config.pass,
        enableSsl = config.enableSsl,
    )
}