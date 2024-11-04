package io.mockative.kontinuity.configuration

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import io.mockative.kontinuity.KONTINUITY_ANNOTATION
import io.mockative.kontinuity.Kontinuity
import io.mockative.kontinuity.KontinuityGeneration
import io.mockative.kontinuity.getAnnotationsByClassName
import io.mockative.kontinuity.getValue
import io.mockative.kontinuity.ksp.ifUnspecified

data class ClassConfiguration(
    val wrapper: String,
    val generation: KontinuityGeneration,
    val suspend: String,
    val suspendFlow: String,
    val flow: String,
    val members: String,
) {
    companion object {
        private val defaults = Kontinuity()

        private fun fromAnnotation(
            annotation: KSAnnotation?,
            parentConfiguration: SourceConfiguration
        ): ClassConfiguration {
            return ClassConfiguration(
                annotation?.getValue("wrapper", defaults.wrapper)
                    ?.ifEmpty { null }
                    ?: parentConfiguration.wrappers,

                annotation?.getValue("generation", defaults.generation)
                    ?.ifUnspecified { parentConfiguration.generation }
                    ?: parentConfiguration.generation,

                annotation?.getValue("suspend", defaults.suspend)
                    ?.ifEmpty { null }
                    ?: parentConfiguration.suspend,

                annotation?.getValue("suspendFlow", defaults.suspendFlow)
                    ?.ifEmpty { null }
                    ?: parentConfiguration.suspendFlow,

                annotation?.getValue("flow", defaults.flow)
                    ?.ifEmpty { null }
                    ?: parentConfiguration.flow,

                annotation?.getValue("members", defaults.members)
                    ?.ifEmpty { null }
                    ?: parentConfiguration.members,
            )
        }

        fun fromDeclaration(
            declaration: KSClassDeclaration,
            parentConfiguration: SourceConfiguration
        ): ClassConfiguration {
            val annotation = declaration.getAnnotationsByClassName(KONTINUITY_ANNOTATION).firstOrNull()
            return fromAnnotation(annotation, parentConfiguration)
        }
    }
}
