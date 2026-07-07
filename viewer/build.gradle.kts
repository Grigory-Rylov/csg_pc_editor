plugins {
    kotlin("jvm")
    id("java")
    application
}

dependencies {
    implementation("com.github.kitakeyos-dev:plugin4j:v1.0.1")
    implementation(project(":cad3d"))
    implementation(project(":javascad"))
    implementation(project(":plugin"))
    implementation(project(":pccase"))
    implementation(project(":config-parser"))

    implementation("com.fifesoft:rsyntaxtextarea:3.6.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")

    implementation("org.jogamp.gluegen:gluegen-rt-main:2.5.0")
    implementation(files("libs/jogl-all-2.5.0.jar"))
    implementation(files("libs/jogl-all-2.5.0-natives-macosx-universal.jar"))

    testImplementation("junit:junit:4.13.1")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testImplementation(kotlin("test"))
}

application {
    mainClass.set("com.github.grishberg.cad3d.MainKt")
}

val joglJvmArgs = listOf(
    "--add-exports=java.desktop/sun.awt=ALL-UNNAMED",
    "--add-opens=java.desktop/sun.awt=ALL-UNNAMED"
)

tasks.withType<JavaExec> {
    jvmArgs(joglJvmArgs)
}

tasks.test {
    systemProperty("java.awt.headless", "true")
    useJUnitPlatform()
    jvmArgs(joglJvmArgs)
}

kotlin {
    jvmToolchain(17)
}
