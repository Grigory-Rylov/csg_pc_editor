plugins {
    kotlin("jvm")
    `java-library`
}

kotlin {
    jvmToolchain(17) // Устанавливаем единую версию Java для всех задач
}
