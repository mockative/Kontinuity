package io.mockative.kontinuity.kotlinpoet

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ksp.toClassName

internal fun KSPropertyDeclaration.toMemberName(): MemberName {
    return when (val parent = parentDeclaration) {
        is KSClassDeclaration -> MemberName(parent.toClassName(), simpleName.asString())
        else -> MemberName(packageName.asString(), simpleName.asString())
    }
}