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

    // Use local JARs — jogamp.org Maven repo is unreachable from CI runners
    implementation(files(
        "libs/jogamp-fat.jar",
        "libs/jogl-all-2.5.0-natives-macosx-universal.jar"
    ))

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

tasks.register<Jar>("fatJar") {
    group = "build"
    description = "Assembles a fat JAR with all dependencies"
    archiveBaseName.set("viewer-all")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest {
        attributes["Main-Class"] = "com.github.grishberg.cad3d.MainKt"
    }
    from(sourceSets.main.get().output)
    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get()
            .filter { it.exists() }
            .map { if (it.isDirectory) it else zipTree(it) }
    })
}
