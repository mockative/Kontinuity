package io.mockative.kontinuity

@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class Kontinuity(
    val name: String = "",
    val platforms: Array<String> = [],
    val jvm: Boolean = false,
    val jvmTargets: Array<String> = [],
    val nativeTargets: Array<String> = []
)

