import io.gitlab.arturbosch.detekt.Detekt
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

val detektVersion: String by project

plugins {
    kotlin("jvm")
    id("org.jlleitschuh.gradle.ktlint")
    id("io.gitlab.arturbosch.detekt")
    jacoco
}

group = "hu.chas.exceldb"
version = "0.1"

repositories {
    mavenCentral()
}

dependencies {
    api("org.apache.poi:poi-ooxml:5.2.3")

    implementation("org.apache.logging.log4j:log4j-core:2.20.0")
    implementation("ch.qos.logback:logback-classic:1.4.7")
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
