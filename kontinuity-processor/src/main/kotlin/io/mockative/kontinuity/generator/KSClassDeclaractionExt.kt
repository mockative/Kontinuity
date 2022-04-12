package io.mockative.kontinuity.generator

import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ksp.toClassName
import io.mockative.kontinuity.Kontinuity
import io.mockative.kontinuity.Options

fun KSClassDeclaration.toNativeInterfaceClassName(): ClassName {
    val format = getAnnotationsByType(Kontinuity::class).firstOrNull()
        ?.interfaceName
        ?.ifEmpty { null }
        ?: Options.Generator.interfaceName

    return toNativeFormattedClassName(format)
}

fun KSClassDeclaration.toNativeWrapperClassName(): ClassName {
    val format = getAnnotationsByType(Kontinuity::class).firstOrNull()
        ?.wrapperClassName
        ?.ifEmpty { null }
        ?: Options.Generator.wrapperClassName

    return toNativeFormattedClassName(format)
}

fun KSClassDeclaration.toNativeFormattedClassName(format: String): ClassName {
    val className = toClassName()
    val prefixes = className.simpleNames.dropLast(1)
    val formattedClassName = format.format(className.simpleNames.last())
    return ClassName(className.packageName, prefixes + formattedClassName)
}