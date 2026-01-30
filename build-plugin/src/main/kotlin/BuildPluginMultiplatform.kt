package ru.otus.otuskotlin.marketplace.plugin

import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.repositories
import org.gradle.kotlin.dsl.the
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension


@Suppress("unused")
internal class BuildPluginMultiplatform : Plugin<Project> {

    override fun apply(project: Project) = with(project) {
        pluginManager.apply("org.jetbrains.kotlin.multiplatform")
        group = rootProject.group
        version = rootProject.version

        plugins.withId("org.jetbrains.kotlin.multiplatform") {
            extensions.configure<KotlinMultiplatformExtension> {
                configureTargets(this@with)
                sourceSets.configureEach {
                    languageSettings.apply {
                        languageVersion = "1.9"
                        progressiveMode = true
                        optIn("kotlin.time.ExperimentalTime")
                    }
                }
            }
        }
        repositories {
            mavenCentral()
        }
    }
}

@Suppress("LongMethod", "MagicNumber")
private fun KotlinMultiplatformExtension.configureTargets(project: Project) {
    val libs = project.the<LibrariesForLibs>()

    jvm {
    }
    linuxX64()
    macosArm64()
    macosX64()
    project.tasks.withType(JavaCompile::class.java) {
        sourceCompatibility = libs.versions.jvm.language.get()
        targetCompatibility = libs.versions.jvm.compiler.get()
    }
}
