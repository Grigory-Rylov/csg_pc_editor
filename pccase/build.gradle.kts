plugins {
    kotlin("jvm")
    id("java")
}

dependencies {
    implementation(project(":javascad"))
    implementation(project(":cad3d"))
}

kotlin {
    jvmToolchain(17)
}
