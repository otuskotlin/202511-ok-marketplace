import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    kotlin("multiplatform")
    id("com.google.devtools.ksp")
    id("io.kotest")
}

kotlin {
    jvm()
    linuxX64()

    val kotestVersion: String by project
    val coroutinesVersion: String by project
    val datetimeVersion: String by project
    val jUnitJupiterVersion: String by project

    sourceSets {
        commonMain {
            dependencies {
                implementation(kotlin("stdlib-common"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))

                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion")

                implementation("io.kotest:kotest-framework-engine:$kotestVersion")
                implementation("io.kotest:kotest-assertions-core:$kotestVersion")

            }
        }
        jvmMain {
            dependencies {
                implementation(kotlin("stdlib"))
            }
        }
        jvmTest {
            dependencies {
                implementation(kotlin("test-junit5"))
                implementation("io.kotest:kotest-runner-junit5-jvm:$kotestVersion")
                implementation("org.junit.jupiter:junit-jupiter-params:$jUnitJupiterVersion")
            }
        }
        nativeMain {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:$datetimeVersion")
            }
        }
    }
}

tasks {
    withType<Test>().configureEach {
        useJUnitPlatform {
//            includeTags.add("sampling")
        }
        filter {
            isFailOnNoMatchingTests = false
        }
        testLogging {
            showExceptions = true
            showStandardStreams = true
            events = setOf(TestLogEvent.FAILED, TestLogEvent.PASSED)
            exceptionFormat = TestExceptionFormat.FULL
        }
    }
}

afterEvaluate {
    tasks.withType<org.jetbrains.kotlin.gradle.targets.jvm.tasks.KotlinJvmTest>().configureEach {
        classpath += files(
            project.layout.buildDirectory.dir("generated/ksp/jvm/jvmTest/kotlin")
        )
    }
}
