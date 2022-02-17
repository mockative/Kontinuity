# KoroutineKit

[ksp]: https://github.com/google/ksp

[![Build](https://github.com/mockative/mockative/actions/workflows/build.yml/badge.svg)](https://github.com/mockative/mockative/actions/workflows/build.yml)
[![Maven Central](https://img.shields.io/maven-central/v/io.mockative/koroutine-kit-processor)](https://search.maven.org/artifact/io.mockative/koroutine-kit-processor)

Effortless use of Kotlin Multiplatform coroutines in Swift, including `suspend` functions and 
`Flow<T>` returning members.

## Installation for Multiplatform projects

KoroutineKit uses the [Kotlin Symbol Processing API][KSP] to process Kotlin code, as well as 
generating Kotlin and Swift code, and as such, it requires adding the KSP plugin in addition to 
adding the runtime library and symbol processor dependencies.

**build.gradle.kts**

```kotlin
plugins {
    id("com.google.devtools.ksp")
}

repositories {
    mavenCentral()
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.mockative:koroutine-kit:1.0.0-SNAPSHOT")
            }
        }
    }
}

dependencies {
    ksp("io.mockative:koroutine-kit-processor:1.0.0-SNAPSHOT")
}
```
