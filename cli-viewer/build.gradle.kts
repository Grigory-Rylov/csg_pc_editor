plugins {
    kotlin("jvm")
    id("java")
    application
}

dependencies {
    implementation(project(":pccase"))
    implementation(project(":javascad"))
    implementation(project(":cad3d"))
    implementation(project(":plugin"))
}

application {
    mainClass.set("com.github.grishberg.cad3d.cli.PcCaseAppKt")
    // Suppress JOGL AppContext reflective access warnings on JDK 17+
    applicationDefaultJvmArgs = listOf(
        "--add-exports=java.desktop/sun.awt=ALL-UNNAMED",
        "--add-opens=java.desktop/sun.awt=ALL-UNNAMED"
    )
}

kotlin {
    jvmToolchain(17)
}
