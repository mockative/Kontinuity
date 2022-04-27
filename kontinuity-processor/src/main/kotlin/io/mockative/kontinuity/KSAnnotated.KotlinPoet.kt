package io.mockative.kontinuity

import com.google.devtools.ksp.symbol.KSAnnotated
import com.squareup.kotlinpoet.AnnotationSpec

internal fun KSAnnotated.getAnnotationSpecs(): List<AnnotationSpec> {
    return annotations
        .mapNotNull { it.getAnnotationSpec() }
        .toList()
}