package io.mockative.kontinuity

import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSTypeReference
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.TypeParameterResolver
import com.squareup.kotlinpoet.ksp.toTypeName

sealed interface ReturnType {
    data class Value(val type: TypeName) : ReturnType
    data class Flow(val elementType: TypeName) : ReturnType
    data class StateFlow(val elementType: TypeName) : ReturnType

    companion object {
        internal fun fromDeclaration(declaration: KSPropertyDeclaration, typeParameterResolver: TypeParameterResolver): ReturnType {
            return fromTypeReference(declaration.type, typeParameterResolver)
        }

        internal fun fromDeclaration(declaration: KSFunctionDeclaration, typeParameterResolver: TypeParameterResolver): ReturnType {
            return fromTypeReference(declaration.returnType!!, typeParameterResolver)
        }

        private fun fromTypeReference(typeReference: KSTypeReference, typeParameterResolver: TypeParameterResolver): ReturnType {
            val typeName = typeReference.toTypeName(typeParameterResolver)

            if (typeName is ParameterizedTypeName) {
                when (typeName.rawType) {
                    STATE_FLOW -> return StateFlow(typeName.typeArguments[0])
                    FLOW, SHARED_FLOW -> return Flow(typeName.typeArguments[0])
                }
            }

            return Value(typeName)
        }
    }
}