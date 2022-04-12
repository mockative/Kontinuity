package io.mockative.kontinuity

@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class Async(
    /**
     * Determines whether an asynchronous Swift function is generated for this member.
     */
    val generate: Boolean = true,
)