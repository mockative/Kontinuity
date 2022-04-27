package io.mockative.kontinuity

import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration

data class MemberConfiguration(
    val name: String,
    val generate: Boolean,
) {
    companion object {
        private fun getGenerate(
            annotation: KontinuityMember?,
            parentConfiguration: ClassConfiguration
        ): Boolean {
            return annotation?.generate ?: when (parentConfiguration.generation) {
                KontinuityGeneration.UNSPECIFIED -> true
                KontinuityGeneration.NONE -> false
                KontinuityGeneration.OPT_OUT -> true
                KontinuityGeneration.OPT_IN -> false
            }
        }

        private fun fromAnnotation(
            annotation: KontinuityMember?,
            returnType: ReturnType,
            parentConfiguration: ClassConfiguration
        ): MemberConfiguration {
            return MemberConfiguration(
                name = annotation?.name?.ifEmpty { null } ?: when (returnType) {
                    is ReturnType.Flow, is ReturnType.StateFlow -> parentConfiguration.flow
                    is ReturnType.Value -> parentConfiguration.members
                    else -> TODO()
                },
                generate = getGenerate(annotation, parentConfiguration),
            )
        }

        fun fromDeclaration(
            declaration: KSPropertyDeclaration,
            returnType: ReturnType,
            parentConfiguration: ClassConfiguration
        ): MemberConfiguration {
            val annotation = declaration.getAnnotationsByType(KontinuityMember::class).firstOrNull()
            return fromAnnotation(annotation, returnType, parentConfiguration)
        }

        private fun fromAnnotation(
            annotation: KontinuityMember?,
            functionType: FunctionType,
            parentConfiguration: ClassConfiguration
        ): MemberConfiguration {
            return MemberConfiguration(
                name = annotation?.name?.ifEmpty { null } ?: when (functionType) {
                    is FunctionType.Suspending -> when (functionType.returnType) {
                        is ReturnType.Flow, is ReturnType.StateFlow -> parentConfiguration.suspendFlow
                        is ReturnType.Value -> parentConfiguration.suspend
                        else -> TODO()
                    }
                    is FunctionType.Blocking -> when (functionType.returnType) {
                        is ReturnType.Flow, is ReturnType.StateFlow -> parentConfiguration.flow
                        is ReturnType.Value -> parentConfiguration.members
                        else -> TODO()
                    }
                    else -> TODO()
                },
                generate = getGenerate(annotation, parentConfiguration),
            )
        }

        fun fromDeclaration(
            declaration: KSFunctionDeclaration,
            functionType: FunctionType,
            parentConfiguration: ClassConfiguration
        ): MemberConfiguration {
            val annotation = declaration.getAnnotationsByType(KontinuityMember::class).firstOrNull()
            return fromAnnotation(annotation, functionType, parentConfiguration)
        }
    }
}