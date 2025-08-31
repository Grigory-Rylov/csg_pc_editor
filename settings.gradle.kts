plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "smooth_surface_opengl"

include("viewer", "cad3d", "javascad", "plugin")

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven { url = uri("https://jitpack.io") }
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
        maven {
            url = uri("https://jogamp.org/deployment/maven/")
        }
    }
}
