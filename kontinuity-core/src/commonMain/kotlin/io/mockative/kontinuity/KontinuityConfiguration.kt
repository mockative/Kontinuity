package io.mockative.kontinuity

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class KontinuityConfiguration(
    val className: String = "",
//    val transformedMemberName: String = "",
//    val simpleMemberName: String = "",
)
