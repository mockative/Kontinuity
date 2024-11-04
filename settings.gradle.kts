pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }

    plugins {
        kotlin("multiplatform") version "2.0.21" apply false
        id("com.google.devtools.ksp") version "2.0.21-1.0.26" apply false
        id("org.jetbrains.kotlin.jvm") version "2.0.21"
    }
}

rootProject.name = "Kontinuity"

if (startParameter.projectProperties.containsKey("check_publication")) {
    include(":tools:check-publication")
} else {
    include(":shared")
    include(":kontinuity-core")
    include(":kontinuity-processor")
}
