plugins {
    kotlin("jvm")
}

dependencies {
    compile(project(":commons"))

    compile(kotlin("stdlib-jdk8"))

    // database
    compile("org.jetbrains.exposed:exposed:0.14.1")

	compile("commons-io:commons-io:2.6")
	compile("xstream:xstream:1.2.2")
    /* bitset */
    compile("com.zaxxer:SparseBitSet:1.2")

    /* htmlcompressor */
    compile(group="com.github.hazendaz", name="htmlcompressor", version="1.6.5")
    
    compile(fileTree("dir" to "../libs", "include" to listOf("*.jar")))
}

sourceSets {
    main {
        resources {
            srcDir("dist")
        }
    }
}

tasks {
    processResources {
        exclude("config", "data", "geodata", "images", "logs", "sql", "*.sh", "*.bat")
    }

    jar {
        archiveFileName.set("gameserver.jar")
        manifest = project.the<JavaPluginConvention>().manifest {
            attributes(Properties.manifest + ("Main-Class" to "l2s.gameserver.GameServer"))
        }
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

distributions {
    getByName("main") {
        val fileName = "game_server_" + Properties.formattedTime
        baseName = fileName

        contents {
            into("lib") {
                from(tasks.named<Jar>("jar"))
                from(configurations.runtime)
            }
            into("") {
                from("dist").exclude("config").exclude("geodata")
            }
            into("config") {
                from("dist/config/")
            }
        }
    }
}
