plugins {
    kotlin("jvm")
    id("java")
    application
}

dependencies {
    implementation(project(":cad3d"))
    implementation(project(":plugin"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
}

application {
    mainClass.set("com.github.grishberg.cad3d.console.ConsoleAppKt")
}

kotlin {
    jvmToolchain(17)
}
