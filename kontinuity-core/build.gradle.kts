plugins {
    id("convention.multiplatform")
    id("convention.publication")
}

group = findProperty("project.group") as String
version = findProperty("project.version") as String

kotlin {
    sourceSets {
        // Common
        commonMain.configure {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
                implementation("org.jetbrains.kotlinx:atomicfu:0.25.0")
            }
        }

        listOf(jsMain, jvmMain, androidMain, wasmJsMain, wasmWasiMain, androidNativeMain, linuxMain, mingwMain)
            .forEach { sourceSet ->
                sourceSet.configure {
                    kotlin.srcDir("src/nonDarwinMain/kotlin")
                }
            }

        all {
            languageSettings {
                optIn("kotlin.RequiresOptIn")
            }
        }
    }
}
