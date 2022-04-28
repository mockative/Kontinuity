package io.mockative.kontinuity.kotlinpoet

import com.google.devtools.ksp.symbol.KSAnnotation
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.MemberName
import io.mockative.kontinuity.KONTINUITY_ANNOTATION

internal fun KSAnnotation.toAnnotationSpec(): AnnotationSpec? {
    val type = annotationType.resolve()

    val className = type.toClassName()!!
    if (className == KONTINUITY_ANNOTATION) {
        return null
    }

    val builder = AnnotationSpec.builder(className)

    arguments.forEach { arg ->
        val name = arg.name!!.asString()
        val memberName = MemberName(className, name)

        when (val value = arg.value) {
            null -> builder.addMember("%M = null", memberName)
            is String -> builder.addMember("%M = %S", memberName, value)
            else -> builder.addMember("%M = %L", memberName, value)
        }
    }

    return builder.build()
}