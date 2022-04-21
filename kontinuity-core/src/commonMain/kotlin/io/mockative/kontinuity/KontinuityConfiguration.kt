package io.mockative.kontinuity

/**
 * Configures Kontinuity Wrapper and member generation on a project/per-target basis.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class KontinuityConfiguration(
    val className: String = "",

    /**
     * Specifies the name of the generated Kontinuity Wrapper class, using the format specifier
     * `%T` as a placeholder for the name of the class or interface this annotation is applied to.
     */
    val wrappers: String = "",

    /**
     * Controls how a Kontinuity Wrapper is generated for the annotated class or interface.
     */
    val generation: KontinuityGeneration = KontinuityGeneration.OPT_OUT,

    /**
     * Specifies the format used when generating members for `suspend` functions in a Kontinuity
     * Wrapper, using `%M` as a placeholder for the name of the member.
     */
    val suspend: String = "",

    /**
     * Specifies the format used when generating members for the `Flow` members in a Kontinuity
     * Wrapper, using `%M` as a placeholder for the name of the member.
     */
    val flow: String = "",

    /**
     * Specifies the format used when generating simple members in the Kontinuity Wrapper of the
     * annotated type, using `%M` as a placeholder for the name of the member.
     */
    val members: String = ""
)
