plugins {
    kotlin("jvm")
    id("java")
    application
}

dependencies {
    implementation(project(":javascad"))
    implementation(project(":cad3d"))
    implementation(project(":plugin"))

    implementation(files("../viewer/libs/jogamp-fat.jar"))
}

application {
    mainClass.set("com.github.grishberg.cad3d.Main")
    applicationDefaultJvmArgs = listOf(
        "--add-exports=java.desktop/sun.awt=ALL-UNNAMED",
        "--add-opens=java.desktop/sun.awt=ALL-UNNAMED"
    )
}

kotlin {
    jvmToolchain(17)
}
