# Kontinuity

[ksp]: https://github.com/google/ksp

[![Build](https://github.com/mockative/mockative/actions/workflows/build.yml/badge.svg)](https://github.com/mockative/mockative/actions/workflows/build.yml)
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

The generated Kontinuity Wrapper can be used in Swift through `KontinuityCore` and one or more of 
`KontinuityCombine`, `KontinuityAsync` or `KontinuityRxSwift`.

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

Both the format of the Kontinuity Wrapper class name and the members within can be controlled
through the use of the `@Kontinuity` and `@KontinuityMember` annotations, the
`@KontinuityConfiguration` annotation, and some KSP arguments.

```kotlin
// Informs the Kontinuity processor to generate a Kontinuity Wrapper class for the class or
// interface this annotation is present on.
annotation class Kontinuity(
    // Specifies the name of the generated Kontinuity Wrapper class, using the format specifier
    // `%T` as a placeholder for the name of the class or interface this annotation is applied to.
    val name: String = "",

    // Controls how a Kontinuity Wrapper is generated for the annotated class or interface.
    val generation: KontinuityGeneration = KontinuityGeneration.OPT_OUT,

    // Specifies the format used when generating coroutine members in the Kontinuity Wrapper of the
    // annotated type, using `%M` as a placeholder for the name of the member.
    val coroutines: String = "",

    // Specifies the format used when generating simple members in the Kontinuity Wrapper of the
    // annotated type, using `%M` as a placeholder for the name of the member.
    val members: String = ""
)

// Controls how a Kontinuity Wrapper is generated for the annotated class or interface.
enum class KontinuityGeneration {
    // Disables generation of a Kontinuity Wrapper.
    NONE,

    // Enables generation of a Kontinuity Wrapper, containing a member for each member of the
    // annotated type, which hasn't otherwise explicitly disabled generation using
    // [KontinuityMember.generate].
    OPT_OUT,

    // Enables generation of a Kontinuity Wrapper, containing a member for each member of the
    // annotated type, which has also explicitly enabled generation using [KontinuityMember].
    OPT_IN,
}
```

```kotlin
// Changes the Kontinuity Wrapper member generating for a specific property or function of a type
// annotated with the [Kontinuity] annotation.
annotation class KontinuityMember(
    // Specifies the name of the generated member, using the format specifier `%M` as a placeholder
    // for the member name.
    val name: String = "",

    // Controls whether a member for the annotated member is generated in the Kontinuity Wrapper.
    // Defaults to `true`.
    val generate: Boolean = true
)
```

```kotlin
// Configures Kontinuity Wrapper and member generation on a project/per-target basis.
annotation class KontinuityConfiguration(
    // Specifies the name of the generated Kontinuity Wrapper class, using the format specifier
    // `%T` as a placeholder for the name of the class or interface this annotation is applied to.
    val wrappers: String = "",

    // Controls how a Kontinuity Wrapper is generated for the annotated class or interface.
    val generation: KontinuityGeneration = KontinuityGeneration.OPT_OUT,

    // Specifies the format used when generating coroutine members in the Kontinuity Wrapper of the
    // annotated type, using `%M` as a placeholder for the name of the member.
    val coroutines: String = "",

    // Specifies the format used when generating simple members in the Kontinuity Wrapper of the
    // annotated type, using `%M` as a placeholder for the name of the member.
    val members: String = ""
)
```

### Swift

### Kotlin (Source)

```kotlin
// Source (Kotlin)
@Kontinuity
interface TaskService {
    val tasks: StateFlow<Task>

    suspend fun refresh()
}
```

### Kotlin (Generated)

```kotlin
// Generated (Kotlin)
open class KTaskService(val wrapped: TaskService) {
    val tasksK: KontinuityStateFlow<Task>
        get() = wrapped.toKontinuityStateFlow()

    fun refreshK(): KontinuitySuspend<Unit> =
        kontinuitySuspend { wrapped.refresh() }
}
```

### Kotlin (Koin)

```kotlin
// Source (Kotlin) - Koin
fun createKontinuityModule() = module {
        factory { createKontinuityWrapper(get<TaskService>()) }
    }
```

## Roadmap

- [ ] Add global `@KontinuityScope` annotation to control the default scope through a
  `@SharedImmutable` global variable.
- [ ] Add type-local `@KontinuityScope` annotation to control the default scope on a per-type basis.
- [ ] Add Swift mock library and generator

## Credits

[KMP-NativeCoroutines]: https://github.com/rickclephas/KMP-NativeCoroutines

Kontinuity is heavily inspired by [rickclephas/KMP-NativeCoroutines][KMP-NativeCoroutines], and is
essentially a Kotlin Symbol Processor version of that Kotlin Compiler Plugin.

## License



