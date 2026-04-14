package ru.otus.otuskotlin.marketplace.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider

@Suppress("unused")
class DockerPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create("docker", DockerExtension::class.java)

        project.afterEvaluate {
            println("=== Docker Plugin Started ===")
            println("Number of images: ${extension.images.registeredNames.size}")

            for (dockerImageName in extension.images.registeredNames) {
                println("Processing image: $dockerImageName")

                // Safety check
                if (!extension.images.images.containsKey(dockerImageName)) {
                    println("ERROR: Image not found in images map!")
                    continue
                }

                val ext = extension.images.images[dockerImageName]!!
                val suffix = dockerImageName.replace(Regex("[^A-Za-z0-9]+"), "")

                val taskName = "dockerBuild$suffix"
                val imgName = ext.imageName.takeIf { it?.isNotBlank() ?: false } ?: project.name

                val taskProvider: TaskProvider<DockerBuildTask> = project.tasks.register(
                    taskName,
                    DockerBuildTask::class.java
                ) {
                    group = "docker"
                    description = "Builds Docker image: $dockerImageName"

                    dockerFile.set(ext.dockerFile)
                    imageName.set(imgName.lowercase())
                    imageTag.set(ext.imageTag)
                    buildContext.set(ext.buildContext)
                    noCache.set(ext.noCache)
                    removeIntermediateContainers.set(extension.removeIntermediateContainers)

                    buildArgs.set(ext.buildArgs)
                }

                if (ext.dependsOnTask != null) {
                    taskProvider.configure {
                        dependsOn(project.tasks.named(ext.dependsOnTask!!))
                    }
                }
                
            }

            println("=== Docker Plugin Completed ===")
        }
    }
}
