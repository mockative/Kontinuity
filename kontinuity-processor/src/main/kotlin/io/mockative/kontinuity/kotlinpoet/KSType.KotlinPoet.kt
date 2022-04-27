package io.mockative.kontinuity.kotlinpoet

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeAlias
import com.squareup.kotlinpoet.ClassName
import io.mockative.kontinuity.kotlinpoet.toClassName

internal fun KSType.toClassName(): ClassName? {
    return when (val declaration = declaration) {
        is KSClassDeclaration -> declaration.toClassName()
        is KSTypeAlias -> declaration.toClassName()
        else -> null
    }
}