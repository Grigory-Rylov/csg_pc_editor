plugins {
    kotlin("jvm")
    id("java")
}

dependencies {
    implementation(project(":javascad"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
}

kotlin {
    jvmToolchain(17)
}
