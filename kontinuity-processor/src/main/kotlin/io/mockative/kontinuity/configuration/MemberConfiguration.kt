package io.mockative.kontinuity.configuration

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import io.mockative.kontinuity.FunctionType
import io.mockative.kontinuity.KONTINUITY_MEMBER_ANNOTATION
import io.mockative.kontinuity.KontinuityGeneration
import io.mockative.kontinuity.KontinuityMember
import io.mockative.kontinuity.ReturnType
import io.mockative.kontinuity.getAnnotationsByClassName
import io.mockative.kontinuity.getValue

data class MemberConfiguration(
    val name: String,
    val generate: Boolean,
) {
    companion object {
        private val defaults = KontinuityMember()

        private fun getGenerate(
            annotation: KSAnnotation?,
            parentConfiguration: ClassConfiguration
        ): Boolean {
            return annotation?.getValue("generate", defaults.generate) ?: when (parentConfiguration.generation) {
                KontinuityGeneration.UNSPECIFIED -> true
                KontinuityGeneration.NONE -> false
                KontinuityGeneration.OPT_OUT -> true
                KontinuityGeneration.OPT_IN -> false
            }
        }

        private fun fromAnnotation(
            annotation: KSAnnotation?,
            returnType: ReturnType,
            parentConfiguration: ClassConfiguration
        ): MemberConfiguration {
            return MemberConfiguration(
                name = annotation?.getValue("name", defaults.name)?.ifEmpty { null } ?: when (returnType) {
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
            val annotation = declaration.getAnnotationsByClassName(KONTINUITY_MEMBER_ANNOTATION).firstOrNull()
            return fromAnnotation(annotation, returnType, parentConfiguration)
        }

        private fun fromAnnotation(
            annotation: KSAnnotation?,
            functionType: FunctionType,
            parentConfiguration: ClassConfiguration
        ): MemberConfiguration {
            return MemberConfiguration(
                name = annotation?.getValue("name", defaults.name)?.ifEmpty { null } ?: when (functionType) {
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
            val annotation = declaration.getAnnotationsByClassName(KONTINUITY_MEMBER_ANNOTATION).firstOrNull()
            return fromAnnotation(annotation, functionType, parentConfiguration)
        }
    }
}
