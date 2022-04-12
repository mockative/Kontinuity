package io.mockative.kontinuity

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Kontinuity(
    val interfaceName: String = "",

    val wrapperClassName: String = ""
)
