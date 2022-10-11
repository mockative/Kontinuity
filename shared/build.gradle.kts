import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    kotlin("multiplatform")
    id("com.android.library")

    id("com.google.devtools.ksp")
}

kotlin {
    // JS
    js {
        browser()
        nodejs()
    }

    // Android
    android()

    // iOS
    val iosTarget: (String, KotlinNativeTarget.() -> Unit) -> KotlinNativeTarget = when {
        System.getenv("SDK_NAME")?.startsWith("iphoneos") == true -> ::iosArm64
        System.getProperty("os.arch") == "aarch64" -> ::iosSimulatorArm64
        else -> ::iosX64
    }

    iosTarget("ios") {
        binaries {
            framework {
                baseName = "shared"
            }
        }
    }

    val macosTarget: (String, KotlinNativeTarget.() -> Unit) -> KotlinNativeTarget = when {
        System.getenv("ARCHS") == "arm64" -> ::macosArm64
        else -> ::macosX64
    }

    macosTarget("macos") {
        binaries {
            framework {
                baseName = "shared"
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                // KotlinX
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")

                // Kontinuity
                implementation(project(":kontinuity-core"))
            }

            kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))

                implementation("io.mockative:mockative:1.2.5")
            }
        }

        val androidAndroidTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
                implementation("junit:junit:4.13.2")
            }
        }

        val androidMain by getting
        val androidTest by getting {
            dependsOn(androidAndroidTest)
        }

        val iosMain by getting {
            kotlin.srcDir("build/generated/ksp/ios/iosMain/kotlin")
        }
        val iosTest by getting

        val jsMain by getting {
            kotlin.srcDir("build/generated/ksp/js/jsMain/kotlin")
        }
        val jsTest by getting {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }

        val macosMain by getting {
            kotlin.srcDir("build/generated/ksp/macos/macosMain/kotlin")
        }
        val macosTest by getting
    }
}

dependencies {
    configurations
        .filter { it.name.startsWith("ksp") && it.name.contains("Test") }
        .forEach {
            add(it.name, "io.mockative:mockative-processor:1.2.5")
        }
}

android {
    compileSdk = 31
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = 21
        targetSdk = 31
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        testInstrumentationRunnerArguments["clearPackageData"] = "true"

        testOptions {
            execution = "ANDROIDX_TEST_ORCHESTRATOR"
        }
    }

    dependencies {
        androidTestImplementation("androidx.test:runner:1.4.0")
        androidTestUtil("androidx.test:orchestrator:1.4.1")
    }
}

dependencies {
    add("kspMacos", project(":kontinuity-processor"))
    add("kspIos", project(":kontinuity-processor"))
    add("kspJs", project(":kontinuity-processor"))
}
