pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }

    plugins {
        kotlin("multiplatform") version "1.6.10" apply false
        id("com.google.devtools.ksp") version "1.6.10-1.0.4" apply false
        id("org.jetbrains.kotlin.jvm") version "1.6.10"
    }
}

rootProject.name = "Kontinuity"

if (startParameter.projectProperties.containsKey("check_publication")) {
    include(":tools:check-publication")
}

include(":shared")
include(":kontinuity-core")
include(":kontinuity-processor")
