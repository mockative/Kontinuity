package io.mockative.kontinuity.generator

import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ksp.toClassName
import io.mockative.kontinuity.Kontinuity
import io.mockative.kontinuity.Options

fun KSClassDeclaration.getWrapperClass(): WrapperClass {
    val format = getAnnotationsByType(Kontinuity::class).firstOrNull()
        ?.name
        ?.ifEmpty { null }
        ?: Options.Generator.wrapperName

    val className = toClassName()
    return WrapperClass(className.toFormattedClassName(format))
}

private fun ClassName.toFormattedClassName(format: String): ClassName =
    ClassName(packageName, getWrapperSimpleName(format))

private fun ClassName.getWrapperSimpleName(format: String) =
    simpleNames.dropLast(1)
        .plus(format.replace("%T", simpleName))
        .joinToString(".")