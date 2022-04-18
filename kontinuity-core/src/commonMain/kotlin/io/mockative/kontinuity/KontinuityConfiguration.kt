package io.mockative.kontinuity

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class KontinuityConfiguration(
    val className: String = "",
    val memberName: String = "",
    val functionName: String = "",
    val propertyName: String = ""
)
