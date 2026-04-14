import org.gradle.kotlin.dsl.registering
import org.testcontainers.containers.ComposeContainer

plugins {
    id("build-docker")
}

docker {
    images.register("migration-pg") {
        buildContext = project.layout.projectDirectory.toString()
        this.imageName = "ok-marketplace-migration-pg"
        imageTag = "${project.version}"
        dockerFile = "src/main/docker/Dockerfile"
    }
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

group = "ru.otus.otuskotlin.marketplace.migration"
version = "0.1.0"

val pgContainer: ComposeContainer by lazy {
    ComposeContainer(
        file("src/test/compose/docker-compose-pg.yml")
    )
        .withExposedService("psql", 5432)
}

tasks {
    val buildImages by registering { -> dependsOn("dockerBuildmigrationpg") }

    val pgDn by registering { ->
        group = "db"
        doFirst {
            println("Stopping PostgreSQL...")
            pgContainer.stop()
            println("PostgreSQL stopped")
        }
    }
    val pgUp by registering { ->
        group = "db"
        doFirst {
            println("Starting PostgreSQL...")
            pgContainer.start()
            println("PostgreSQL started at port: ${pgContainer.getServicePort("psql", 5432)}")
        }
        finalizedBy(pgDn)
    }
}
