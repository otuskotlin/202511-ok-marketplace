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

val csContainer: ComposeContainer by lazy {
    ComposeContainer(
        file("src/test/compose/docker-compose-cs.yml")
    )
        .withExposedService("cassandra", 9042)
}

tasks {
    val buildImages by creating {
        dependsOn(dockerBuild)
    }

    val cassandraDn by creating {
        group = "db"
        doFirst {
            println("Stopping Cassandra...")
            csContainer.stop()
            println("Cassandra stopped")
        }
    }
    val cassandraUp by creating {
        group = "db"
        doFirst {
            println("Starting Cassandra...")
            csContainer.start()
            println("Cassandra started at port: ${csContainer.getServicePort("cassandra", 9042)}")
        }
        finalizedBy(cassandraDn)
    }

}
