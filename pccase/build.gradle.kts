plugins {
    kotlin("jvm")
    id("java")
}

dependencies {
    implementation(project(":javascad"))
}

kotlin {
    jvmToolchain(17)
}
