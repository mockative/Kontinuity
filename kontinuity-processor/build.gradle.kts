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
                implementation("com.google.devtools.ksp:symbol-processing-api:2.0.21-1.0.26")

                // KotlinPoet
                implementation("com.squareup:kotlinpoet:1.18.1")
                implementation("com.squareup:kotlinpoet-ksp:1.18.1")

                // Kotlin
                implementation(kotlin("reflect"))
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
