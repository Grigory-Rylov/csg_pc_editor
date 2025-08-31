plugins {
    kotlin("jvm")
    `java-library`
    kotlin("plugin.serialization") version "1.9.0"
}

dependencies {
    implementation("com.github.kitakeyos-dev:plugin4j:v1.0.1")

    implementation(project(":javascad"))
    implementation(project(":plugin"))

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")

    testImplementation("junit:junit:4.13.1")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(17) // Устанавливаем единую версию Java для всех задач
}
