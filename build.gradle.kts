import io.gitlab.arturbosch.detekt.Detekt
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

val detektVersion: String by project

plugins {
    kotlin("jvm")
    id("org.jlleitschuh.gradle.ktlint")
    id("io.gitlab.arturbosch.detekt")
    jacoco
}

group = "hu.chas"
version = "0.1"

repositories {
    mavenCentral()
}

dependencies {
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:$detektVersion")
    testImplementation(kotlin("test"))
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
    config = files("config/detekt/detekt.yml")
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
