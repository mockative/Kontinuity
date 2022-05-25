pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }

    plugins {
        kotlin("multiplatform") version "1.6.20" apply false
        id("com.google.devtools.ksp") version "1.7.0-RC-1.0.5" apply false
        id("org.jetbrains.kotlin.jvm") version "1.6.20"
    }
}

rootProject.name = "Kontinuity"

if (startParameter.projectProperties.containsKey("check_publication")) {
    include(":tools:check-publication")
}

include(":shared")
include(":kontinuity-core")
include(":kontinuity-processor")
