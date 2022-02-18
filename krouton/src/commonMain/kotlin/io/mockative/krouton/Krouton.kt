package io.mockative.krouton

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Krouton(
    /**
     * Determines whether Kroutons are generated for the annotated type.
     *
     * Defaults to `true`.
     */
    val generate: Boolean = true,

    /**
     * Name of the resulting Swift/Objective-C module the type annotated with this property is a
     * part of.
     *
     * The default value is an empty string, which results in using the `krouton.darwin.moduleName`
     * KSP option, or falling back to `"shared"` if that is not specified.
     */
    val moduleName: String = "",

    /**
     * The package of the generated file (relative to the generated build output).
     *
     * The default value is an empty string, which results in the package name of the type being
     * used.
     */
    val outputPackage: String = ""
)
