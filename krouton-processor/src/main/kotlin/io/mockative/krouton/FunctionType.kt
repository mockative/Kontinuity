package io.mockative.krouton

import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.Modifier
import com.squareup.kotlinpoet.ksp.TypeParameterResolver

sealed interface FunctionType {
    data class Blocking(val returnType: ReturnType) : FunctionType
    data class Suspending(val returnType: ReturnType) : FunctionType
}

internal fun KSFunctionDeclaration.getFunctionType(typeParameterResolver: TypeParameterResolver): FunctionType {
    val returnType = getReturnType(typeParameterResolver)

    return when {
        modifiers.contains(Modifier.SUSPEND) -> FunctionType.Suspending(returnType)
        else -> FunctionType.Blocking(returnType)
    }
}