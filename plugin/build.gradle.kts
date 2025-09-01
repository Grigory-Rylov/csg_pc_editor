plugins {
    kotlin("jvm")
    `java-library`
    kotlin("plugin.serialization") version "1.9.22"
}
dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
}

kotlin {
    jvmToolchain(17) // Устанавливаем единую версию Java для всех задач
}
