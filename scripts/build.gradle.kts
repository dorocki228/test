dependencies {

    implementation(project(":commons"))
    implementation(project(":gameserver"))

    implementation(fileTree("dir" to "../libs", "include" to listOf("*.jar")))

    /* network */
    implementation(Libs.vertx_core)
    implementation(Libs.vertx_lang_kotlin)

    /* json */
    implementation(Libs.gson)

    /* utils */
    implementation(Libs.guava)
    implementation(Libs.net_sf_trove4j_core)
    implementation(Libs.commons_collections4)
    implementation(Libs.commons_lang3)
    implementation(Libs.commons_io)
    implementation(Libs.commons_text)
}

tasks {
    jar {
        archiveFileName.set("scripts.jar")
        manifest = project.the<JavaPluginConvention>().manifest {
            attributes(Properties.manifest)
        }
    }

    val copyJar by registering(Copy::class) {
        from(named<Jar>("jar"))
        into("../libs")
    }

    build {
        dependsOn(copyJar)
    }
}