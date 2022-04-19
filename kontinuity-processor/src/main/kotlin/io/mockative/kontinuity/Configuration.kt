package io.mockative.kontinuity

object Configuration {
    var source: KontinuityConfiguration? = null

    val className: String?
        get() = source?.className?.ifEmpty { null }

//    val transformedMemberName: String?
//        get() = source?.transformedMemberName?.ifEmpty { null }
//
//    val simpleMemberName: String?
//        get() = source?.simpleMemberName?.ifEmpty { null }
}