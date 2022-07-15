plugins {
    kotlin("jvm")
    application
}

apply(plugin="kotlinx-serialization")

dependencies {
    implementation(project(":commons"))

    implementation(Libs.kotlin_stdlib_jdk8)

    /* coroutines */
    implementation(Libs.kotlinx_coroutines_core)

    /* configuration */
    implementation(Libs.config)

    implementation(fileTree("dir" to "../libs", "include" to listOf("*.jar")))

    /* database */
    implementation(Libs.hikaricp)
    implementation(Libs.exposed)

    /* bitset */
    implementation(Libs.roaringbitmap)

    /* htmlcompressor */
    implementation(Libs.htmlcompressor)

    /* network */
    implementation(Libs.netty_common)
    implementation(Libs.netty_buffer)
    implementation(Libs.netty_transport_native_epoll)
    implementation(Libs.vertx_core)
    implementation(Libs.vertx_lang_kotlin)

    /* cache */
    implementation(Libs.caffeine)

    /* serialization */
    implementation(Libs.dom4j)
    implementation("org.jdom:jdom2:2.0.6")
    implementation(Libs.kotlinx_serialization_runtime)
    implementation(Libs.kaml)

    /* jaxb */
    implementation(Libs.jaxb_api)
    implementation(Libs.jaxb_impl)
    implementation(Libs.jaxb_core)
    implementation(Libs.jaxb_runtime)
    implementation(Libs.activation)

    /* logging */
    implementation(Libs.flogger)
    implementation(Libs.google_extensions)
    runtime(Libs.flogger_system_backend)
    runtime(Libs.flogger_slf4j_backend)
    runtime(Libs.slf4j_api)
    runtime(Libs.log4j_api)
    runtime(Libs.log4j_core)
    runtime(Libs.log4j_slf4j18_impl)

    /* template engine */
    implementation(Libs.velocity_engine_core)

    /* json */
    implementation(Libs.gson)

    /* utils */
    implementation(Libs.guava)
    implementation(Libs.net_sf_trove4j_core)
    implementation(Libs.commons_collections4)
    implementation(Libs.commons_lang3)
    implementation(Libs.commons_io)
    implementation(Libs.commons_text)
    runtime(Libs.disruptor)
}

tasks {
    jar {
        archiveFileName.set("gameserver.jar")
        manifest = project.the<JavaPluginConvention>().manifest {
            attributes(Properties.manifest + ("Main-Class" to "org.strixplatform.StrixPlatform"))
        }
    }

    startScripts {
        classpath = files("*")
        (unixStartScriptGenerator as TemplateBasedScriptGenerator).template = resources.text.fromFile("unixStartScript.txt")
    }

    withType<JavaExec> {
        //debug = true
        //classpath = sourceSets["main"].runtimeClasspath + sourceSets["main"].resources
    }

    distZip {
        dependsOn(":scripts:build")

        val fileName = "gameserver_" + Properties.formattedTime
        archiveFileName.set("$fileName.zip")
    }
    distTar {
        enabled = false
    }
}

application {
    mainClassName = "org.strixplatform.StrixPlatform"
    applicationDefaultJvmArgs = listOf(
        "-server",
        "-Xms4g",
        "-Xmx15g",
        "-Dfile.encoding=UTF-8",
        "-Dflogger.backend_factory=com.google.common.flogger.backend.slf4j.Slf4jBackendFactory#getInstance"
    )
    executableDir = ""
}