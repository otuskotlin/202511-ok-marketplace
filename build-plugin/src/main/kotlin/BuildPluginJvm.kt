package ru.otus.otuskotlin.marketplace.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.repositories
import org.gradle.kotlin.dsl.the
import org.gradle.accessors.dm.LibrariesForLibs // Для доступа к libs
import org.gradle.api.tasks.compile.JavaCompile

@Suppress("unused")
internal class BuildPluginJvm : Plugin<Project> {

    override fun apply(project: Project) {
        val libs = project.the<LibrariesForLibs>() // Достаем каталог зависимостей

        project.pluginManager.apply("org.jetbrains.kotlin.jvm")
        project.group = project.rootProject.group
        project.version = project.rootProject.version

        project.tasks.withType(JavaCompile::class.java) {
            sourceCompatibility = libs.versions.jvm.language.get()
            targetCompatibility = libs.versions.jvm.compiler.get()
        }

        project.repositories {
            mavenCentral()
        }
    }
}
