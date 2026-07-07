plugins {
    kotlin("jvm")
    `java-library`
}

dependencies {
    implementation(project(":pccase"))
    implementation(project(":javascad"))

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
}

kotlin {
    jvmToolchain(17)
}
