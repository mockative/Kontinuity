package io.mockative.kontinuity

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.FileLocation
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import io.mockative.kontinuity.kotlinpoet.toMemberName

fun KSAnnotated.getAnnotationsByClassName(name: ClassName): Sequence<KSAnnotation> {
    return annotations.filter { it.annotationType.toTypeName() == name }
}

@Suppress("UNCHECKED_CAST")
fun <T> KSAnnotation.getValue(name: String, defaultValue: T): T {
    val argument = arguments.firstOrNull { it.name!!.asString() == name } ?: return defaultValue
    return argument.value as T
}

object DefaultKontinuityScopeDeclaration {
    private fun multipleDefaultScopes(defaultScopes: List<KSPropertyDeclaration>): String {
        return buildString {
            appendLine("More than one top-level property was annotated as the default @KontinuityScope. ")
            appendLine()
            appendLine("  The annotated properties were: ")

            defaultScopes.forEach { defaultScope ->
                val memberName = defaultScope.toMemberName()
                val location = (defaultScope.location as? FileLocation)
                    ?.let { file -> "(${file.filePath}:${file.lineNumber})" } ?: ""

                appendLine("    $memberName$location")
            }

            appendLine()
            appendLine("  Only one property in a compilation unit may be annotated with `@KontinuityScope(default = true)`")
        }
    }

    fun fromResolver(resolver: Resolver): KSPropertyDeclaration? {
        val defaultScopes = resolver.getSymbolsWithAnnotation(KONTINUITY_SCOPE_ANNOTATION.canonicalName)
            .mapNotNull { it as? KSPropertyDeclaration }
            .filter { it.parentDeclaration == null }
            .filter { it.getAnnotationsByClassName(KONTINUITY_SCOPE_ANNOTATION).single().getValue("default", false) }
            .toList()

        if (defaultScopes.size > 1) {
            throw KontinuityGeneratorException(multipleDefaultScopes(defaultScopes))
        }

        return defaultScopes.firstOrNull()
    }
}
