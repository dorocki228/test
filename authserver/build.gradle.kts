plugins {
    kotlin("jvm")
}

dependencies {
    compile(project(":commons"))

    compile(kotlin("stdlib-jdk8"))

    compile(files("../libs/jacksum-1.7.0.jar"))
}

tasks {
    withType<Jar> {
        archiveFileName.set("authserver.jar")
        manifest = project.the<JavaPluginConvention>().manifest {
            attributes(Properties.manifest + ("Main-Class" to "l2s.authserver.AuthServer"))
        }
    }

    distZip {
        val fileName = "authserver_" + Properties.formattedTime
        archiveFileName.set("$fileName.zip")
    }
    distTar {
        enabled = false
    }
}

distributions {
    getByName("main") {
        val fileName = "auth_server_" + Properties.formattedTime
        baseName = fileName

        contents {
            into("lib") {
                from(tasks.named<Jar>("jar"))
                from(configurations.runtime)
            }
            into("") {
                from("dist").exclude("config")
            }
            into("config") {
                from("dist/config/default/")
            }
        }
    }
}
