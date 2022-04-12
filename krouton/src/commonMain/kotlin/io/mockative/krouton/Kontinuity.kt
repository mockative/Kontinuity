package io.mockative.krouton

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Kontinuity(
    val interfaceName: String = "",

    val wrapperClassName: String = ""
)
