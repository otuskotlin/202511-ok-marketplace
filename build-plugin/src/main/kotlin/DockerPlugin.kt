package ru.otus.otuskotlin.marketplace.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

class DockerPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create("docker", DockerExtension::class.java)

        project.tasks.register("dockerBuild", DockerBuildTask::class.java) {
            dockerFile.set(extension.dockerFile)
            imageName.set(extension.imageName)
            imageTag.set(extension.imageTag)
            buildContext.set(extension.buildContext)
            buildArgs.set(extension.buildArgs)
            noCache.set(extension.noCache)
            removeIntermediateContainers.set(extension.removeIntermediateContainers)
        }
    }
}