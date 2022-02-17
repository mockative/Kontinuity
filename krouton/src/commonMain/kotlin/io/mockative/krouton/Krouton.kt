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
     * The package of the generated file (relative to the generated build output).
     *
     * The default value is an empty string, which results in the package name of the type being
     * used.
     */
    val outputPackage: String = ""
)
