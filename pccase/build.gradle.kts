plugins {
    kotlin("jvm")
    id("java")
    application
}

dependencies {
    implementation(project(":javascad"))
    implementation(project(":cad3d"))
    implementation(project(":plugin"))
}

application {
    mainClass.set("com.github.grishberg.cad3d.pccase.PcCaseAppKt")
}

kotlin {
    jvmToolchain(17)
}
