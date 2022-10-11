# Kontinuity

[ksp]: https://github.com/google/ksp

[![Build](https://github.com/mockative/Kontinuity/actions/workflows/build.yml/badge.svg)](https://github.com/mockative/Kontinuity/actions/workflows/build.yml)
[![Maven Central](https://img.shields.io/maven-central/v/io.mockative/kontinuity-processor)](https://search.maven.org/artifact/io.mockative/kontinuity-processor)

Effortless use of Kotlin Multiplatform coroutines in Swift, including `suspend` functions and
`Flow<T>` returning members.

## Installation

Kontinuity includes both a core Kotlin library, a [Kotlin Symbol Processor][KSP], and various Swift
Package Manager packages, depending on how you want to consume the generated code. Please make sure
to use the same versions of each individual part.

### Kotlin (Gradle)

In your Kotlin Multiplatform module, add the following to your __build.gradle.kts__ file:

```kotlin
plugins {
    id("com.google.devtools.ksp")
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.mockative:kontinuity-core:1.0.0-SNAPSHOT")
            }
        }
    }
}

dependencies {
    add("kspIos", "io.mockative:kontinuity-processor:1.0.0-SNAPSHOT")
}
```

### Swift (Swift Package Manager)

The Swift libraries are available using Swift Package Manager (SPM), by adding the following to
your __Package.swift__ file, or in your Xcode Project's Package Dependencies.

```swift
dependencies: [
    .package(url: "https://github.com/mockative/Kontinuity.git", from: "<version>")
]
```

## Usage

For each Kotlin class or interface annotated with `@Kontinuity`, a wrapper class is generated,
referred to as the "Kontinuity Wrapper".

Given a Kotlin interface like the following:

```kotlin
package com.app.sample.tasks

@Kontinuity
interface TaskService {
    val tasks: StateFlow<List<Task>>

    suspend fun createTask(task: Task)

    fun close()
}
```

A Kontinuity Wrapper will be generated:

```kotlin
package com.app.sample.tasks

// The Kontinuity Wrapper class takes the prefix 'K' by default
open class KTaskService(private val wrapped: TaskService) {
    // Coroutine members have their names transformed with the 'K' suffix by default
    val tasksK: KontinuityStateFlow<Task>
        get() = wrapped.toKontinuityStateFlow()

    fun refreshK(): KontinuitySuspend<Unit> =
        kontinuitySuspend { wrapped.refresh() }

    // Simple members don't require a name transformation 
    fun close() =
        wrapped.close()
}
```

### Swift

The generated Kontinuity Wrapper can be used in Swift through `KontinuityCore` and `KontinuityCombine`.

```swift
import KontinuityCore
import KontinuityCombine

val kotlinTaskService: TaskService
val taskService = KTaskService(wrapped: kotlinTaskService)

// Accessing the current value of a StateFlow
val tasks = getValue(of: taskService.tasksK)

// Subscribing to a Flow
val subscription = createPublisher(for: taskService.tasksK)
    .sink { completion in } receiveValue: { tasks in
        print("tasks: \(tasks)")
    } 

// Calling a suspend function
val subscription = createFuture(for: taskService.refreshK())
    .sink { completion in
        print("refresh: \(completion)")
    } receiveValue: { unit in }
```

### Name Transformations

Kontinuity Wrapper classes have their names transformed from the type they're wrapping, by prefixing
the class with `K`. Coroutine members of Kontinuity Wrapper classes also have their names
transformed, in order to prevent the Kotlin compiler from suffixing it with `_` when compiling for
iOS/Darwin, which it does to prevent member signature clashes when interfaces are used.

See [Configuration - Name Transformations](CONFIGURATION.md#name-transformations) for more 
information in how to configure Kontinuity.

### Coroutine Scope

By default, Kontinuity launches all coroutines in a scope using `Dispatchers.Main.immediate`. You 
can override this behaviour by annotating a top-level property with `@KontinuityScope`. This 
annotation can both be applied to the entire source set (by specifying `default = true`), or to the 
`@Kontinuity` annotated types within a single source file:

```kotlin
// Specifies the coroutine scope used to launch Kontinuity coroutines of types within this source 
// set(s), unless otherwise overwritten by a file-level @KontinuityScope.
@SharedImmutable
@KontinuityScope(default = true)
internal val defaultKontinuityScope = CoroutineScope(Dispatchers.Default + SuperviserJob())
```

```kotlin
// Specifies the coroutine scope used to launch Kontinuity coroutines of types within this file.
@SharedImmutable
@KontinuityScope
internal val taskServiceScope = CoroutineScope(Dispatchers.Unconfined + SuperviserJob())

interface TaskService {
    // Coroutines launches within this type (and other types in this file) are launched in the 
    // `taskServiceScope`.
}
```

## Roadmap

- [ ] Add Swift mock library and generator
- [X] Add global `@KontinuityScope` annotation to control the default scope through a
  `@SharedImmutable` global variable.
- [X] Add type-local `@KontinuityScope` annotation to control the default scope on a per-type basis.
- [X] ~~Consider rewriting `SharedFlow<T>` wrapper generation to generating 2 properties, one for the 
  flow, one for the value `%MValue`.~~
    - Implementing this breaks usage of `suspend` functions returning `StateFlow<T>`. 

## Credits

[KMP-NativeCoroutines]: https://github.com/rickclephas/KMP-NativeCoroutines

Kontinuity is heavily inspired by [rickclephas/KMP-NativeCoroutines][KMP-NativeCoroutines], and is
essentially a Kotlin Symbol Processor version of that Kotlin Compiler Plugin, born out of a desire 
to have similar features while maintaining compatibility with KSP.
