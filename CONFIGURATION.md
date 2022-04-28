# Configuration

## Name Transformations

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