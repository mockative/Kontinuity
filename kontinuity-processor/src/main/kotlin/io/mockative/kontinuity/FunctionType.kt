package io.mockative.kontinuity

import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.Modifier
import com.squareup.kotlinpoet.ksp.TypeParameterResolver

sealed interface FunctionType {
    data class Blocking(val returnType: ReturnType) : FunctionType
    data class Suspending(val returnType: ReturnType) : FunctionType

    companion object {
        internal fun fromDeclaration(declaration: KSFunctionDeclaration, typeParameterResolver: TypeParameterResolver): FunctionType {
            val returnType = ReturnType.fromDeclaration(declaration, typeParameterResolver)

            return when {
                declaration.modifiers.contains(Modifier.SUSPEND) -> Suspending(returnType)
                else -> Blocking(returnType)
            }
        }
    }
}
