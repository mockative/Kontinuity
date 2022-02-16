pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }

    plugins {
        kotlin("multiplatform") version "1.6.0" apply false
        id("com.google.devtools.ksp") version "1.6.0-1.0.1" apply false
    }
}

rootProject.name = "krouton"

include(":shared")
include(":krouton")
include(":krouton-processor")
include(":krouton-test")
include(":krouton-code-generator")

if (startParameter.projectProperties.containsKey("check_publication")) {
    include(":tools:check-publication")
}
