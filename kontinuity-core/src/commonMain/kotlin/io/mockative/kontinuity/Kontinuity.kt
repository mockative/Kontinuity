package io.mockative.kontinuity

@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class Kontinuity(
    val name: String = "",
    val generate: Boolean = true
)
