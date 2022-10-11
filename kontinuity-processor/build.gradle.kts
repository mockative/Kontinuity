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
                // Kontinuity
                implementation(project(":kontinuity-core"))

                // KSP
                implementation("com.google.devtools.ksp:symbol-processing-api:1.7.20-1.0.6")

                // KotlinPoet
                implementation("com.squareup:kotlinpoet:1.12.0")
                implementation("com.squareup:kotlinpoet-ksp:1.11.0")
            }

            kotlin.srcDir("src/main/kotlin")
            resources.srcDir("src/main/resources")
        }

        all {
            languageSettings {
                optIn("com.google.devtools.ksp.KspExperimental")
                optIn("com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview")
                optIn("kotlin.time.ExperimentalTime")
            }
        }
    }
}
