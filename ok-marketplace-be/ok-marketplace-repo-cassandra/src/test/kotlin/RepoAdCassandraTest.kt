package ru.otus.otuskotlin.marketplace.backend.repo.cassandra

import com.benasher44.uuid.uuid4
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Ignore
import org.junit.experimental.runners.Enclosed
import org.junit.runner.RunWith
import org.testcontainers.containers.ComposeContainer
import org.testcontainers.containers.output.Slf4jLogConsumer
import org.testcontainers.containers.wait.strategy.Wait
import ru.otus.otuskotlin.marketplace.backend.repo.tests.*
import ru.otus.otuskotlin.marketplace.repo.common.AdRepoInitialized
import java.io.File
import java.time.Duration

@Suppress("unused")
@RunWith(Enclosed::class)
class CassandraTest {

    class RepoAdCassandraCreateTest : RepoAdCreateTest() {
        override val repo = AdRepoInitialized(
            initObjects = initObjects,
            repo = repository(uuidNew.asString())
        )
    }

    class RepoAdCassandraReadTest : RepoAdReadTest() {
        override val repo = AdRepoInitialized(
            initObjects = initObjects,
            repo = repository()
        )
    }

    class RepoAdCassandraUpdateTest : RepoAdUpdateTest() {
        override val repo = AdRepoInitialized(
            initObjects = initObjects,
            repo = repository(lockNew.asString())
        )
    }

    class RepoAdCassandraDeleteTest : RepoAdDeleteTest() {
        override val repo = AdRepoInitialized(
            initObjects = initObjects,
            repo = repository()
        )
    }

    class RepoAdCassandraSearchTest : RepoAdSearchTest() {
        override val repo = AdRepoInitialized(
            initObjects = initObjects,
            repo = repository()
        )
    }

    @Ignore
    companion object {
        private const val CS_SERVICE = "cassandra"
        private const val CS_PORT = 9042
        private const val MG_SERVICE = "liquibase"

         val LOGGER = org.slf4j.LoggerFactory.getLogger(ComposeContainer::class.java)
        private val container: ComposeContainer by lazy {
            val resDc = this::class.java.classLoader.getResource("docker-compose-cs.yml")
                ?: throw Exception("No resource found")
            val fileDc = File(resDc.toURI())
              val logConsumer = Slf4jLogConsumer(LOGGER)
            ComposeContainer(
                fileDc,
            )
                .withExposedService(CS_SERVICE, CS_PORT)
                .withStartupTimeout(Duration.ofMinutes(10))
                .withLogConsumer(CS_SERVICE, logConsumer)
                .withLogConsumer(MG_SERVICE, logConsumer)
//                .withLogConsumer(PG_SERVICE, logConsumer)
                .waitingFor(
                    MG_SERVICE,
                    Wait.forLogMessage(".*Liquibase command 'update' was executed successfully.*", 1)
                    //Wait.defaultWaitStrategy().withStartupTimeout(Duration.ofSeconds(5000))
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
        fun start() {
            container.start()
        }

        @JvmStatic
        @AfterClass
        fun finish() {
            container.stop()
        }
    }
}
