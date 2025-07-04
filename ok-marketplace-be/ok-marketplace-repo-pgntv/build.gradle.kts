import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeTest
import org.testcontainers.containers.ComposeContainer
import org.testcontainers.containers.output.Slf4jLogConsumer
import org.testcontainers.containers.wait.strategy.Wait
import java.time.Duration

plugins {
    id("build-kmp")
}
buildscript {
    repositories {
        mavenCentral()
    }

    dependencies {
        // Testcontainers core + Docker Compose модуль
        // classpath("org.testcontainers:testcontainers:1.20.6")
        classpath(libs.testcontainers.core)
    }
}

kotlin {
    sourceSets {
        linuxX64Main {
            dependencies {
                implementation(kotlin("stdlib"))

                implementation(projects.okMarketplaceCommon)
                api(projects.okMarketplaceRepoCommon)

                implementation(libs.coroutines.core)
                implementation(libs.uuid)

                implementation("io.github.moreirasantos:pgkn:1.1.0")
            }
        }
        linuxX64Test {
            dependencies {
                implementation(projects.okMarketplaceRepoTests)
            }
        }
    }
}

@Suppress("PropertyName")
val PG_SERVICE = "psql"
val MG_SERVICE = "liquibase"
val LOGGER = org.slf4j.LoggerFactory.getLogger(ComposeContainer::class.java)

val pgContainer: ComposeContainer by lazy {
    val logConsumer = Slf4jLogConsumer(LOGGER)
    ComposeContainer(
        file("src/docker/docker-compose-pg.yml")
    )
        .withLocalCompose(true)
        .withExposedService(PG_SERVICE, 5432)
        .withStartupTimeout(Duration.ofSeconds(300))
        .withLogConsumer(MG_SERVICE, logConsumer)
        .withLogConsumer(PG_SERVICE, logConsumer)
        .waitingFor(
            MG_SERVICE,
            Wait.forLogMessage(".*Liquibase command 'update' was executed successfully.*", 1)
        )
}

tasks {
    val pgDn by creating {
        group = "db"
        doFirst {
            println("Stopping PostgreSQL...")
            pgContainer.stop()
            println("PostgreSQL stopped")
        }
    }
    val pgUp by creating {
        group = "db"
        doFirst {
            println("Starting PostgreSQL...")
            runCatching { pgContainer.start() }.getOrElse {
                it.printStackTrace()
            }
            println("PostgreSQL started at port: ${pgContainer.getServicePort(PG_SERVICE, 5432)}")
        }
        finalizedBy(pgDn)
    }

    withType<KotlinNativeTest> {
        dependsOn(pgUp)
        finalizedBy(pgDn)
        doFirst {
            environment = mapOf(
                "postgresPort" to pgContainer.getServicePort(PG_SERVICE, 5432)
            )
        }
    }
}