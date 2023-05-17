pluginManagement {
    val detektVersion: String by settings
    val kotlinVersion: String by settings
    val ktlintVersion: String by settings

    plugins {
        kotlin("jvm") version kotlinVersion apply false
        id("org.jlleitschuh.gradle.ktlint") version ktlintVersion apply false
        id("io.gitlab.arturbosch.detekt") version detektVersion apply false
    }

    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}
rootProject.name = "ExcelDB"
