pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "yi4k-remote-android"
include(":app")

// Camera protocol library, developed as a sibling repo/checkout at "../yi4k-sdk".
// Once published (e.g. via JitPack), this can be replaced by a regular Maven coordinate
// in app/build.gradle.kts instead of a composite build.
includeBuild("../yi4k-sdk") {
    dependencySubstitution {
        substitute(module("com.adidoo.yi4k:sdk")).using(project(":"))
    }
}
