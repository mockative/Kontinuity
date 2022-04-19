package io.mockative.kontinuity

@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class Kontinuity(
    val name: String = "K%T",
    val platforms: Array<String> = [],
    val jvm: Boolean = false,
    val jvmTargets: Array<String> = [],
    val nativeTargets: Array<String> = []
)

object Platform {
    const val jvm = "jvm"
    const val js = "js"
    const val ios = "ios"
    const val macos = "macos"
}
