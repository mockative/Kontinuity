plugins {
    id("convention.multiplatform")
    id("convention.publication")
}

group = findProperty("project.group") as String
version = findProperty("project.version") as String

kotlin {
    @Suppress("UNUSED")
    sourceSets {
        // Common
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0-native-mt")
            }

            kotlin.srcDirs("$buildDir/generated/krouton-code-generator")
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
