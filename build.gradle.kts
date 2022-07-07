import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.40" apply false
    distribution
}

allprojects {
    group = "gve"
    version = "1.0"

    repositories {
        mavenCentral()
        jcenter()
    }
}

subprojects {
    version = "1.0"

    apply(plugin="java")
    apply(plugin="distribution")

    configure<JavaPluginConvention> {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    tasks {
        withType<JavaCompile> {
            options.compilerArgs = listOf("-parameters")
            options.encoding = "UTF-8"
        }

        withType<KotlinCompile> {
            kotlinOptions {
                jvmTarget = "11"
                apiVersion = "1.3"
                languageVersion = "1.3"
                freeCompilerArgs = listOf(
                    "-progressive",
                    "-XXLanguage:+NewInference",
                    "-XXLanguage:+SamConversionForKotlinFunctions",
                    "-XXLanguage:+InlineClasses",
                    "-Xallow-result-return-type",
                    "-Xuse-experimental=kotlin.ExperimentalUnsignedTypes",
                    "-Xno-param-assertions",
                    "-Xno-call-assertions"
                )
            }
        }
    }
}

tasks {
    distZip {
        dependsOn(project(":gameserver").tasks["distZip"], project(":authserver").tasks["distZip"])

        val fileName = "server_" + Properties.formattedTime
        archiveFileName.set("$fileName.zip")
    }
    distTar {
        enabled = false
    }

    build {
        doLast {
            println("Build in build/distributions/" + getByName<Zip>("distZip").archiveFileName.get())
        }
    }

    wrapper {
        gradleVersion = "5.4.1"
    }
}

distributions {
    getByName("main") {
        contents {
            into("server") {
                from(project(":gameserver").tasks["distZip"])
                from(project(":authserver").tasks["distZip"])
            }
        }
    }
}