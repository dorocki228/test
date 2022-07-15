plugins {
    kotlin("jvm")
}

dependencies {

    implementation(project(":commons"))
    implementation(project(":gameserver"))

    implementation(Libs.kotlin_stdlib_jdk8)

    /* serialization */
    implementation(Libs.kotlinx_serialization_runtime)
    implementation(Libs.kaml)

    /* utils */
    implementation(Libs.guava)

}