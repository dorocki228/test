plugins {
    kotlin("jvm")
}

dependencies {
    compile(kotlin("stdlib-jdk8"))

    /* configuration */
    compile("org.aeonbits.owner:owner-java8:1.0.10")
    compile("org.aeonbits.owner:owner-java8-extras:1.0.10")

    /* network */
    compile(group="io.netty", name="netty-all", version="4.1.36.Final")
    compile("io.vertx:vertx-core:3.7.1")
    compile("io.vertx:vertx-lang-kotlin:3.7.1")

    /* database */
    compile(group="com.zaxxer", name="HikariCP", version="3.3.1")
    compile(group="org.mariadb.jdbc", name="mariadb-java-client", version="2.4.1")
    compile(group="org.springframework", name="spring-jdbc", version="5.1.7.RELEASE")
    compile("org.jdbi:jdbi3-core:3.8.2")
    compile("org.jdbi:jdbi3-sqlobject:3.8.2")

    /* logging */
    compile(group="org.slf4j", name="slf4j-api", version="1.8.0-beta4")
    compile(group="org.apache.logging.log4j", name="log4j-api", version="2.11.2")
    compile(group="org.apache.logging.log4j", name="log4j-core", version="2.11.2")
    compile(group="org.apache.logging.log4j", name="log4j-slf4j18-impl", version="2.11.2")

    /* cache */
    compile(group="com.github.ben-manes.caffeine", name="caffeine", version="2.7.0")

    /* xml */
    compile(group="org.dom4j", name="dom4j", version="2.1.1")
    compile("com.thoughtworks.xstream:xstream:1.4.11.1")

    /* time */
    compile(group="com.cronutils", name="cron-utils", version="8.1.1")

    // compiler
    compile("org.eclipse.jdt:org.eclipse.jdt.compiler.tool:1.2.600")
    compile("org.eclipse.jdt:org.eclipse.jdt.compiler.apt:1.3.600")

    /* utils */
    compile(group="com.google.guava", name="guava", version="28.0-jre")
    compile(group="org.apache.commons", name="commons-lang3", version="3.9")
    compile("org.apache.commons:commons-text:1.6")
    compile(group="org.apache.velocity", name="velocity-engine-core", version="2.1")
    compile("org.jooq:jool:0.9.14")
    compile(group="com.lmax", name="disruptor", version="3.4.2")
}

tasks {
    jar {
        archiveFileName.set("commons.jar")
        manifest = project.the<JavaPluginConvention>().manifest {
            attributes(Properties.manifest)
        }
    }
}
