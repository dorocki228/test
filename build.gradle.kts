import net.ltgt.gradle.errorprone.errorprone
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        jcenter()
    }

    dependencies {
        classpath(Libs.kotlin_gradle_plugin)
        classpath(Libs.kotlin_serialization)
    }
}

plugins {
    kotlin("jvm") version Versions.org_jetbrains_kotlin apply false
    id("net.ltgt.errorprone") version "0.8.1"
    buildSrcVersions
}

allprojects {
    group = "remorse"
    version = "1.0"

    repositories {
        mavenCentral()
        jcenter()
        maven { setUrl("https://dl.bintray.com/hotkeytlt/maven") }
    }
}

subprojects {
    version = "1.0"

    apply(plugin="java")
    apply(plugin="net.ltgt.errorprone")

    dependencies {
        // errorprone javac only required for Java 8
        errorproneJavac("com.google.errorprone:javac:9+181-r4173-1")
        // error prone itself
        errorprone("com.google.errorprone:error_prone_core:2.3.3")
        // custom error prone plugin
        errorprone("com.digitalascent:digitalascent-errorprone-flogger:0.8.1")
    }

    configure<JavaPluginConvention> {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    tasks {
        withType<JavaCompile> {
            options.compilerArgs = listOf("-parameters")
            options.encoding = "UTF-8"

            options.errorprone {
                // change source logging API as appropriate
                option("LoggerApiRefactoring:SourceApi", "slf4j")
                //errorproneArgs.add("-XepPatchChecks:MissingOverride,DefaultCharset,DeadException")
                //errorproneArgs.add("-XepPatchChecks:MissingOverride,DeadException")
                //errorproneArgs.add("-XepPatchChecks:LoggerApiRefactoring")
                //errorproneArgs.add("-XepPatchLocation:IN_PLACE")
                errorproneArgs.add("-XepDisableAllChecks")
            }
        }

        withType<KotlinCompile> {
            kotlinOptions {
                jvmTarget = "11"
                apiVersion = "1.4"
                languageVersion = "1.4"
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
    wrapper {
        gradleVersion = Versions.gradleLatestVersion
        distributionType = Wrapper.DistributionType.ALL
    }
}