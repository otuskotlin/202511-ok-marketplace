package ru.otus.otuskotlin.marketplace.app.ktor.repo

import com.benasher44.uuid.uuid4
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import org.testcontainers.containers.ComposeContainer
import org.testcontainers.containers.wait.strategy.Wait
import ru.otus.otuskotlin.marketplace.api.v1.models.AdRequestDebugMode
import ru.otus.otuskotlin.marketplace.app.ktor.MkplAppSettings
import ru.otus.otuskotlin.marketplace.backend.repo.cassandra.RepoAdCassandra
import ru.otus.otuskotlin.marketplace.common.MkplCorSettings
import ru.otus.otuskotlin.marketplace.common.repo.IRepoAd
import ru.otus.otuskotlin.marketplace.repo.common.AdRepoInitialized
import java.io.File
import java.time.Duration

class V1AdRepoCassandraTest : V1AdRepoBaseTest() {
    override val workMode: AdRequestDebugMode = AdRequestDebugMode.TEST
    private fun mkAppSettings(repo: IRepoAd) = MkplAppSettings(
        corSettings = MkplCorSettings(
            repoTest = repo
        )
    )

    override val appSettingsCreate: MkplAppSettings = mkAppSettings(
        repo = AdRepoInitialized(repository(uuidNew))
    )
    override val appSettingsRead: MkplAppSettings = mkAppSettings(
        repo = AdRepoInitialized(
            repository(),
            initObjects = listOf(initAd),
        )
    )
    override val appSettingsUpdate: MkplAppSettings = mkAppSettings(
        repo = AdRepoInitialized(
            repository(uuidNew),
            initObjects = listOf(initAd),
        )
    )
    override val appSettingsDelete: MkplAppSettings = mkAppSettings(
        repo = AdRepoInitialized(
            repository(),
            initObjects = listOf(initAd),
        )
    )
    override val appSettingsSearch: MkplAppSettings = mkAppSettings(
        repo = AdRepoInitialized(
            repository(),
            initObjects = listOf(initAd),
        )
    )
    override val appSettingsOffers: MkplAppSettings = mkAppSettings(
        repo = AdRepoInitialized(
            repository(),
            initObjects = listOf(initAd, initAdSupply),
        )
    )

    @Test
    fun cassandraV1Test() {
        println("Cassandra v2")
    }

    companion object {
        private const val CS_SERVICE = "cassandra"
        private const val CS_PORT = 9042
        private const val MG_SERVICE = "liquibase"

        // val LOGGER = org.slf4j.LoggerFactory.getLogger(ComposeContainer::class.java)
        private val container: ComposeContainer by lazy {
            val resDc = this::class.java.classLoader.getResource("docker-compose-cs.yml")
                ?: throw Exception("No resource found")
            val fileDc = File(resDc.toURI())
            //  val logConsumer = Slf4jLogConsumer(LOGGER)
            ComposeContainer(
                fileDc,
            )
                .withExposedService(CS_SERVICE, CS_PORT)
                .withStartupTimeout(Duration.ofSeconds(300))
//                .withLogConsumer(MG_SERVICE, logConsumer)
//                .withLogConsumer(PG_SERVICE, logConsumer)
                .waitingFor(
                    MG_SERVICE,
                    Wait.forLogMessage(".*Liquibase command 'update' was executed successfully.*", 1)
                )
        }

        fun repository(uuid: String? = null): RepoAdCassandra {
            return RepoAdCassandra(
                keyspaceName = "marketplace",
                host = container.getServiceHost(CS_SERVICE, CS_PORT),
                port = container.getServicePort(CS_SERVICE, CS_PORT),
                randomUuid = uuid?.let { { uuid } } ?: { uuid4().toString() },
                dc = "dc1",
            ).apply { clear() }
        }

        @JvmStatic
        @BeforeClass
        fun tearUp() {
            container.start()
        }

        @JvmStatic
        @AfterClass
        fun tearDown() {
            container.stop()
        }
    }
}
