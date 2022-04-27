package io.mockative.kontinuity

import com.google.devtools.ksp.isLocal
import com.google.devtools.ksp.symbol.KSDeclaration
import com.squareup.kotlinpoet.ClassName

internal fun KSDeclaration.toClassName(): ClassName {
    require(!isLocal()) {
        "Local/anonymous classes are not supported!"
    }
    val pkgName = packageName.asString()
    val typesString = checkNotNull(qualifiedName).asString().removePrefix("$pkgName.")

    val simpleNames = typesString
        .split(".")
    return ClassName(pkgName, simpleNames)
}