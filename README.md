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

### Swift

```swift
// Source (Swift)
struct TaskListView: View {
    @Environment(\.taskService) private var taskService: KTaskService
    
    @State private var tasks: [Task]? = nil
    @State private var tasksSubscription: AnyCancellable? = nil
    
    @State private var refreshSubscription: AnyCancellable? = nil
    
    var body: some View {
        List(tasks ?? getValue(of: taskService.tasksK)) { task in
            TaskListItem(task)
        }
        .onAppear {
            tasksSubscription = createPublisher(for: taskService.tasksK)
                .sink { _ in } receiveValue: { tasks in
                    self.tasks = tasks
                }
        
            refreshSubscription = createFuture(for: taskService.refreshK())
                .sink { _ in } receiveValue: { _ in }
        }
    }
}
```

### Kotlin (Koin)

```kotlin
// Source (Kotlin) - Koin
fun createKontinuityModule() = module {
    factory { createKontinuityWrapper(get<TaskService>()) }
}
```