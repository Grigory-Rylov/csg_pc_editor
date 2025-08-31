plugins {
    id("java")
    kotlin("jvm")
}

group = "eu.printingin3d.javascad"
version = "1.0"

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation(kotlin("stdlib-jdk8"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17) // Устанавливаем единую версию Java для всех задач
}
