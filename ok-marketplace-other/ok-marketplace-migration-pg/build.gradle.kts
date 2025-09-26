import org.testcontainers.containers.ComposeContainer

plugins {
    id("build-docker")
}

docker {
    imageName = project.name
    dockerFile = "src/main/docker/Dockerfile"
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
    val buildImages by creating {
        dependsOn(dockerBuild)
    }

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
            pgContainer.start()
            println("PostgreSQL started at port: ${pgContainer.getServicePort("psql", 5432)}")
        }
        finalizedBy(pgDn)
    }
}
