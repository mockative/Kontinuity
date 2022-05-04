plugins {
    id("convention.multiplatform")
    id("convention.publication")

    id("com.android.library")
}

group = findProperty("project.group") as String
version = findProperty("project.version") as String

kotlin {
    android()

    sourceSets {
        // Common
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0-native-mt")
            }
        }

        val androidMain by getting {
            dependencies {
                implementation("androidx.lifecycle:lifecycle-viewmodel:2.4.1")
                implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.4.1")
                implementation("androidx.lifecycle:lifecycle-livedata:2.4.1")
            }
        }

        all {
            languageSettings {
                optIn("kotlin.RequiresOptIn")
            }
        }
    }
}

afterEvaluate {
    kotlin.targets["metadata"].compilations.forEach { compilation ->
        compilation.compileKotlinTask.doFirst {
            compilation.compileDependencyFiles = files(
                compilation.compileDependencyFiles.filterNot { it.absolutePath.endsWith("klib/common/stdlib") }
            )
        }
    }
}
