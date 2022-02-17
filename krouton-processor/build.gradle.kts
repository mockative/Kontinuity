plugins {
    kotlin("multiplatform")
    id("convention.publication")
}

group = findProperty("project.group") as String
version = findProperty("project.version") as String

kotlin {
    jvm()

    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(project(":krouton"))

                implementation("com.google.devtools.ksp:symbol-processing-api:1.6.10-1.0.2")

                implementation("com.squareup:kotlinpoet:1.10.2")
                implementation("com.squareup:kotlinpoet-ksp:1.10.2")
            }

            kotlin.srcDir("src/main/kotlin")
            resources.srcDir("src/main/resources")
        }

        all {
            languageSettings {
                optIn("com.google.devtools.ksp.KspExperimental")
                optIn("com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview")
            }
        }
    }
}
