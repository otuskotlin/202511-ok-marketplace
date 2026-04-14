package ru.otus.otuskotlin.marketplace.backend.repo.postgresql

import com.benasher44.uuid.uuid4
import io.github.moreirasantos.pgkn.PostgresDriver
import io.github.moreirasantos.pgkn.resultset.ResultSet
import kotlinx.coroutines.runBlocking
import ru.otus.otuskotlin.marketplace.backend.repo.postgresql.SqlFields.quoted
import ru.otus.otuskotlin.marketplace.common.models.*
import ru.otus.otuskotlin.marketplace.common.repo.*
import ru.otus.otuskotlin.marketplace.repo.common.IRepoAdInitializable

class RepoAdSql(
    properties: SqlProperties = SqlProperties(),
    val randomUuid: () -> String = { uuid4().toString() },
) : IRepoAd, IRepoAdInitializable {
    init {
        require(properties.database.matches(Regex("^[\\w\\d_]+\$"))) {
            "PostgreSQL database must contain only letters, numbers and underscore symbol '_'"
        }
        require(properties.schema.matches(Regex("^[\\w\\d_]+\$"))) {
            "PostgreSQL schema must contain only letters, numbers and underscore symbol '_'"
        }
        require(properties.table.matches(Regex("^[\\w\\d_]+\$"))) {
            "PostgreSQL table must contain only letters, numbers and underscore symbol '_'"
        }
    }

    private val dbName: String = "\"${properties.schema}\".\"${properties.table}\"".apply {
        // Валидируем, что админ не ошибся в имени таблицы
    }

    init {
        initConnection(properties)
    }

    // insert into db (id) VALUES (:id) returning id
    private suspend fun saveElement(saveAd: MkplAd): IDbAdResponse {
        val sql = """
                INSERT INTO $dbName (
                  ${SqlFields.ID.quoted()},
                  ${SqlFields.TITLE.quoted()},
                  ${SqlFields.DESCRIPTION.quoted()},
                  ${SqlFields.VISIBILITY.quoted()},
                  ${SqlFields.AD_TYPE.quoted()},
                  ${SqlFields.LOCK.quoted()},
                  ${SqlFields.OWNER_ID.quoted()},
                  ${SqlFields.PRODUCT_ID.quoted()}
                ) VALUES (
                  :${SqlFields.ID},
                  :${SqlFields.TITLE},
                  :${SqlFields.DESCRIPTION},
                  :${SqlFields.VISIBILITY}::${SqlFields.VISIBILITY_TYPE},
                  :${SqlFields.AD_TYPE}::${SqlFields.AD_TYPE_TYPE},
                  :${SqlFields.LOCK},
                  :${SqlFields.OWNER_ID},
                  :${SqlFields.PRODUCT_ID}
                )
                RETURNING ${SqlFields.allFields.joinToString()}
            """.trimIndent()
        val res = driver.execute(
            sql = sql,
            saveAd.toDb(),
        ) { row: ResultSet -> row.fromDb(SqlFields.allFields) }
        return DbAdResponseOk(res.first())
    }

    override fun save(ads: Collection<MkplAd>): Collection<MkplAd> = runBlocking {
        ads.map {
            val res = saveElement(it)
            if (res !is DbAdResponseOk) throw Exception()
            res.data
        }
    }

    override suspend fun createAd(rq: DbAdRequest): IDbAdResponse {
        val saveAd = rq.ad.copy(id = MkplAdId(randomUuid()), lock = MkplAdLock(randomUuid()))
        return saveElement(saveAd)
    }

    override suspend fun readAd(rq: DbAdIdRequest): IDbAdResponse {
        val sql = """
                SELECT ${SqlFields.allFields.joinToString { it.quoted() }}
                FROM $dbName
                WHERE ${SqlFields.ID.quoted()} = :${SqlFields.ID}
            """.trimIndent()
        val res = driver.execute(
            sql = sql,
            rq.id.toDb(),
        ) { row: ResultSet -> row.fromDb(SqlFields.allFields) }
        return if (res.isEmpty()) errorNotFound(rq.id) else DbAdResponseOk(res.first())
    }

    override suspend fun updateAd(rq: DbAdRequest): IDbAdResponse {
        val sql = """
            WITH update_obj AS (
                UPDATE $dbName a
                SET ${SqlFields.TITLE.quoted()} = :${SqlFields.TITLE}
                , ${SqlFields.DESCRIPTION.quoted()} = :${SqlFields.DESCRIPTION}
                , ${SqlFields.AD_TYPE.quoted()} = :${SqlFields.AD_TYPE}::${SqlFields.AD_TYPE_TYPE}
                , ${SqlFields.VISIBILITY.quoted()} = :${SqlFields.VISIBILITY}::${SqlFields.VISIBILITY_TYPE}
                , ${SqlFields.LOCK.quoted()} = :${SqlFields.LOCK}
                , ${SqlFields.OWNER_ID.quoted()} = :${SqlFields.OWNER_ID}
                , ${SqlFields.PRODUCT_ID.quoted()} = :${SqlFields.PRODUCT_ID}
                WHERE  a.${SqlFields.ID.quoted()} = :${SqlFields.ID}
                AND a.${SqlFields.LOCK.quoted()} = :${SqlFields.LOCK_OLD}
                RETURNING ${SqlFields.allFields.joinToString()}
            ),
            select_obj AS (
                SELECT ${SqlFields.allFields.joinToString()} FROM $dbName
                WHERE ${SqlFields.ID.quoted()} = :${SqlFields.ID}
            )
            (SELECT * FROM update_obj UNION ALL SELECT * FROM select_obj) LIMIT 1
        """.trimIndent()
        val rqAd = rq.ad
        val newAd = rqAd.copy(lock = MkplAdLock(randomUuid()))
        val res = driver.execute(
            sql = sql,
            newAd.toDb() + rqAd.lock.toDb(),
        ) { row: ResultSet -> row.fromDb(SqlFields.allFields) }
        val returnedAd: MkplAd? = res.firstOrNull()
        return when {
            returnedAd == null -> errorNotFound(rq.ad.id)
            returnedAd.lock == newAd.lock -> DbAdResponseOk(returnedAd)
            else -> errorRepoConcurrency(returnedAd, rqAd.lock)
        }
    }

    override suspend fun deleteAd(rq: DbAdIdRequest): IDbAdResponse {
        val sql = """
            WITH delete_obj AS (
                DELETE FROM $dbName a
                WHERE  a.${SqlFields.ID.quoted()} = :${SqlFields.ID}
                AND a.${SqlFields.LOCK.quoted()} = :${SqlFields.LOCK_OLD}
                RETURNING '${SqlFields.DELETE_OK}'
            )
            SELECT ${SqlFields.allFields.joinToString()}, (SELECT * FROM delete_obj) as flag FROM $dbName
            WHERE ${SqlFields.ID.quoted()} = :${SqlFields.ID}
        """.trimIndent()
        val res = driver.execute(
            sql = sql,
            rq.id.toDb() + rq.lock.toDb(),
        ) { row: ResultSet -> row.fromDb(SqlFields.allFields) to row.getString(SqlFields.allFields.size) }
        val returnedPair: Pair<MkplAd,String?>? = res.firstOrNull()
        val returnedAd: MkplAd? = returnedPair?.first
        return when {
            returnedAd == null -> errorNotFound(rq.id)
            returnedPair.second == SqlFields.DELETE_OK -> DbAdResponseOk(returnedAd)
            else -> errorRepoConcurrency(returnedAd, rq.lock)
        }
    }

    override suspend fun searchAd(rq: DbAdFilterRequest): IDbAdsResponse {
        val where = listOfNotNull(
            rq.ownerId.takeIf { it != MkplUserId.NONE }
                ?.let { "${SqlFields.OWNER_ID.quoted()} = :${SqlFields.OWNER_ID}" },
            rq.dealSide.takeIf { it != MkplDealSide.NONE }
                ?.let { "${SqlFields.AD_TYPE.quoted()} = :${SqlFields.AD_TYPE}::${SqlFields.AD_TYPE_TYPE}" },
            rq.titleFilter.takeIf { it.isNotBlank() }
                ?.let { "${SqlFields.TITLE.quoted()} LIKE :${SqlFields.TITLE}" },
        )
            .takeIf { it.isNotEmpty() }
            ?.let { "WHERE ${it.joinToString(separator = " AND ")}" }
            ?: ""

        val sql = """
                SELECT ${SqlFields.allFields.joinToString { it.quoted() }}
                FROM $dbName $where
            """.trimIndent()
        println("SQL: $sql")
        val res = driver.execute(
            sql = sql,
            rq.toDb(),
        ) { row: ResultSet -> row.fromDb(SqlFields.allFields) }
        return DbAdsResponseOk(res)
    }

    fun clear(): Unit = runBlocking {
        val sql = """
                DELETE FROM $dbName
            """.trimIndent()
        driver.execute(sql = sql)
    }

    companion object {
        private lateinit var driver: PostgresDriver
        private fun initConnection(properties: SqlProperties) {
            if (!this::driver.isInitialized) {
                driver = PostgresDriver(
                    host = properties.host,
                    port = properties.port,
                    user = properties.user,
                    database = properties.database,
                    password = properties.password,
                )
            }
        }
    }
}
