dependencies {
    compile(project(":commons"))
    compile(project(":gameserver"))
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