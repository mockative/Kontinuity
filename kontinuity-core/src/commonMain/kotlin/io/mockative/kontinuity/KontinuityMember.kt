package io.mockative.kontinuity

/**
 * Changes the Kontinuity Wrapper member generating for a specific property or function of a type
 * annotated with the [Kontinuity] annotation.
 */
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class KontinuityMember(
    /**
     * Specifies the name of the generated member, using the format specifier `%M` as a placeholder
     * for the member name.
     */
    val name: String = "",

    /**
     * Controls whether a member for the annotated member is generated in the Kontinuity Wrapper.
     * Defaults to `true`.
     */
    val generate: Boolean = true
)