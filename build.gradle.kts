import io.gitlab.arturbosch.detekt.Detekt
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

val detektVersion: String by project

plugins {
    kotlin("jvm")
    id("org.jlleitschuh.gradle.ktlint")
    id("io.gitlab.arturbosch.detekt")
    id("org.jetbrains.dokka")
    jacoco
    `java-library`
    `maven-publish`
}

group = "hu.chas"
version = "0.10"

repositories {
    mavenCentral()
}

dependencies {
    api("org.apache.poi:poi-ooxml:5.2.5")

    implementation("org.apache.logging.log4j:log4j-core:2.22.1")
    implementation("ch.qos.logback:logback-classic:1.5.6")
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")

    implementation(kotlin("reflect"))

    testImplementation(kotlin("test"))

    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:$detektVersion")
}

tasks {
    test {
        useJUnitPlatform()
        testLogging {
            events("standardOut", "started", "passed", "skipped", "failed")
        }
        finalizedBy("jacocoTestReport")
    }

    withType<Detekt>().configureEach {
        reports {
            xml.required.set(true)
            html.required.set(true)
            txt.required.set(true)
            sarif.required.set(true)
            md.required.set(true)
        }
    }

    jacocoTestReport {
        dependsOn(":test")
        reports {
            xml.required.set(false)
            csv.required.set(true)
            html.required.set(true)
            html.outputLocation.set(layout.buildDirectory.dir("jacoco"))
            csv.outputLocation.set(layout.buildDirectory.file("jacoco/jacocoTestReport.csv"))
        }
    }

    jacocoTestCoverageVerification {
        dependsOn(":test")
        violationRules {
            rule {
                limit {
                    minimum = BigDecimal.valueOf(0.90)
                }
            }
        }
    }
}

kotlin {
    jvmToolchain(11)
}

detekt {
    toolVersion = detektVersion
    config.setFrom("config/detekt/detekt.yml")
    buildUponDefaultConfig = true
}

ktlint {
    verbose.set(true)
    outputToConsole.set(true)
    coloredOutput.set(true)
    reporters {
        reporter(ReporterType.CHECKSTYLE)
        reporter(ReporterType.JSON)
        reporter(ReporterType.HTML)
    }
    filter {
        exclude("**/style-violations.kt")
    }
}

val dokkaJavadocJar by tasks.register<Jar>("dokkaJavadocJar") {
    dependsOn(tasks.dokkaJavadoc)
    from(tasks.dokkaJavadoc.flatMap { it.outputDirectory })
    archiveClassifier.set("javadoc")
}

val dokkaHtmlJar by tasks.register<Jar>("dokkaHtmlJar") {
    dependsOn(tasks.dokkaHtml)
    from(tasks.dokkaHtml.flatMap { it.outputDirectory })
    archiveClassifier.set("html-doc")
}

java {
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])

            artifact(dokkaJavadocJar)
            artifact(dokkaHtmlJar)
        }
    }
}
