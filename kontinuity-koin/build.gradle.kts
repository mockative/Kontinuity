plugins {
    kotlin("multiplatform")
    id("convention.publication")
}

kotlin {
    jvm()

    js {
        browser()
        nodejs()
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    watchosArm32()
    watchosArm64()
    watchosX86()
    watchosX64()
    watchosSimulatorArm64()

    tvosArm64()
    tvosX64()
    tvosSimulatorArm64()

    macosX64()
    macosArm64()

    linuxX64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.insert-koin:koin-core:3.1.6")
            }
        }
    }
}
