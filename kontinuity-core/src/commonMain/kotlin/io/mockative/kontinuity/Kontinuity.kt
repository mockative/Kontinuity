package io.mockative.kontinuity

/**
 * Informs the Kontinuity processor to generate a Kontinuity Wrapper class for the class or
 * interface this annotation is present on.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Kontinuity(
    /**
     * Specifies the name of the generated Kontinuity Wrapper class, using the format specifier
     * `%T` as a placeholder for the name of the class or interface this annotation is applied to.
     */
    val name: String = "",

    /**
     * Controls how a Kontinuity Wrapper is generated for the annotated class or interface.
     */
    val generation: KontinuityGeneration = KontinuityGeneration.OPT_OUT,

    /**
     * Specifies the format used when generating members for `suspend` functions in the Kontinuity
     * Wrapper of the annotated type, using `%M` as a placeholder for the name of the member.
     */
    val suspend: String = "",

    /**
     * Specifies the format used when generating members for the `Flow` members in the Kontinuity
     * Wrapper of the annotated type, using `%M` as a placeholder for the name of the member.
     */
    val flow: String = "",

    /**
     * Specifies the format used when generating simple members in the Kontinuity Wrapper of the
     * annotated type, using `%M` as a placeholder for the name of the member.
     */
    val members: String = ""
)
