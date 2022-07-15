plugins {
    kotlin("jvm")
    application
}

dependencies {

    implementation(project(":commons"))

    implementation(Libs.kotlin_stdlib_jdk8)

    implementation(files("../libs/jacksum-1.7.0.jar"))
    //implementation(fileTree("dir" to "../libs", "include" to listOf("*.jar")))

    /* database */
    implementation(Libs.hikaricp)

    /* network */
    implementation(Libs.netty_common)
    implementation(Libs.netty_buffer)
    implementation(Libs.vertx_core)
    implementation(Libs.vertx_lang_kotlin)

    /* logging */
    implementation(Libs.flogger)
    runtime(Libs.flogger_system_backend)
    runtime(Libs.flogger_slf4j_backend)
    runtime(Libs.slf4j_api)
    runtime(Libs.log4j_api)
    runtime(Libs.log4j_core)
    runtime(Libs.log4j_slf4j18_impl)

    /* serialization */
    implementation(Libs.dom4j)

    /* utils */
    implementation(Libs.commons_lang3)
    runtime(Libs.disruptor)
}

tasks {
    withType<Jar> {
        archiveFileName.set("authserver.jar")
        manifest = project.the<JavaPluginConvention>().manifest {
            attributes(Properties.manifest + ("Main-Class" to "l2s.authserver.AuthServer"))
        }
    }

    startScripts {
        classpath = files("*")
    }

    distZip {
        val fileName = "authserver_" + Properties.formattedTime
        archiveFileName.set("$fileName.zip")
    }
    distTar {
        enabled = false
    }
}

application {
    mainClassName = "l2s.authserver.AuthServer"
    applicationDefaultJvmArgs = listOf(
        "-server",
        "-Xmx512m",
        "-Dfile.encoding=UTF-8",
        "-Dflogger.backend_factory=com.google.common.flogger.backend.slf4j.Slf4jBackendFactory#getInstance"
    )
    executableDir = ""
}