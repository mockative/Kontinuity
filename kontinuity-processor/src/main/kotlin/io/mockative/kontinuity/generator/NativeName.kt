package io.mockative.kontinuity.generator

import com.google.devtools.ksp.symbol.KSClassifierReference
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import io.mockative.kontinuity.FunctionType
import io.mockative.kontinuity.Options
import io.mockative.kontinuity.ReturnType

fun KSPropertyDeclaration.getNativeName(type: ReturnType): String {
    return when (type) {
        is ReturnType.Value ->
            Options.Generator.getSimpleMemberName(simpleName.asString())

        is ReturnType.Flow, is ReturnType.StateFlow ->
            Options.Generator.getTransformedMemberName(simpleName.asString())

        else -> throw IllegalStateException("Unknown return type ${type::class}")
    }
}

fun KSFunctionDeclaration.getNativeName(type: FunctionType): String {
    return when (type) {
        is FunctionType.Blocking -> when (type.returnType) {
            is ReturnType.Value ->
                Options.Generator.getSimpleMemberName(simpleName.asString())

            is ReturnType.Flow, is ReturnType.StateFlow ->
                Options.Generator.getTransformedMemberName(simpleName.asString())

            else -> throw IllegalStateException("Unknown return type ${type.returnType::class}")
        }

        is FunctionType.Suspending -> Options.Generator.getTransformedMemberName(simpleName.asString())

        else -> throw IllegalStateException("Unknown function type ${type::class}")
    }
}

fun KSFunctionDeclaration.isFromAny() =
    when (simpleName.asString()) {
        "hashCode", "toString" -> typeParameters.isEmpty() && parameters.isEmpty()
        else -> when {
            isMostLikelyAnyEquals() -> true
            else -> false
        }
    }

private fun KSFunctionDeclaration.isMostLikelyAnyEquals(): Boolean {
    if (simpleName.asString() != "equals") return false
    if (typeParameters.isNotEmpty()) return false
    if (parameters.size != 1) return false

    val parameter = parameters[0]
    if (parameter.name?.asString() != "other") return false

    val parameterType = parameter.type.element
    if (parameterType !is KSClassifierReference) return false
    if (parameterType.referencedName() != "Any") return false

    return true
}
