plugins {
    kotlin("jvm")
}

dependencies {
    implementation(Libs.kotlin_stdlib_jdk8)

    /* coroutines */
    implementation(Libs.kotlinx_coroutines_core)

    /* configuration */
    implementation(Libs.owner_java8)
    implementation(Libs.owner_java8_extras)

    /* network */
    implementation(Libs.netty_buffer)
    implementation(Libs.vertx_core)
    implementation(Libs.vertx_lang_kotlin)

    /* database */
    implementation(Libs.hikaricp)
    implementation(Libs.mariadb_java_client)

    /* logging */
    implementation(Libs.flogger)
    implementation(Libs.google_extensions)
    runtime(Libs.slf4j_api)
    runtime(Libs.log4j_api)

    /* cache */
    implementation(Libs.caffeine)

    /* serialization */
    implementation(Libs.dom4j)
    implementation("org.jdom:jdom2:2.0.6")

    /* time */
    implementation(Libs.cron_utils)

    // compiler
    implementation(Libs.org_eclipse_jdt_compiler_tool)
    implementation(Libs.org_eclipse_jdt_compiler_apt)

    /* utils */
    implementation(Libs.guava)
    implementation(Libs.net_sf_trove4j_core)
    implementation(Libs.commons_collections4)
    implementation(Libs.commons_lang3)
    implementation(Libs.commons_io)
    implementation(Libs.commons_text)
    implementation(Libs.juniversalchardet)
}

tasks {
    jar {
        archiveFileName.set("commons.jar")
        manifest = project.the<JavaPluginConvention>().manifest {
            attributes(Properties.manifest)
        }
    }
}