import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    kotlin("multiplatform")
    id("com.android.library")

    id("com.google.devtools.ksp")
}

kotlin {
    js {
        browser()
        nodejs()
    }

    android()

    val iosTarget: (String, KotlinNativeTarget.() -> Unit) -> KotlinNativeTarget = when {
        System.getenv("SDK_NAME")?.startsWith("iphoneos") == true -> ::iosArm64
        System.getenv("NATIVE_ARCH")?.startsWith("arm") == true -> ::iosSimulatorArm64
        else -> ::iosSimulatorArm64
    }

    iosTarget("ios") {
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
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0-native-mt")

                // Kontinuity
                implementation(project(":kontinuity-core"))
            }

            kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        val androidAndroidTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
                implementation("junit:junit:4.13.2")
            }
        }

        // Suppresses warnings
        val androidAndroidTestRelease by getting
        val androidTestFixtures by getting
        val androidTestFixturesDebug by getting { dependsOn(androidTestFixtures) }
        val androidTestFixturesRelease by getting { dependsOn(androidTestFixtures) }

        val androidMain by getting
        val androidTest by getting {
            // Suppresses warnings
            dependsOn(androidAndroidTest)
            dependsOn(androidAndroidTestRelease)
            dependsOn(androidTestFixturesDebug)
            dependsOn(androidTestFixturesRelease)
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
        androidTestUtil("androidx.test:orchestrator:1.4.0")
    }
}

dependencies {
    add("kspIos", project(":kontinuity-processor"))
    add("kspJs", project(":kontinuity-processor"))
}
