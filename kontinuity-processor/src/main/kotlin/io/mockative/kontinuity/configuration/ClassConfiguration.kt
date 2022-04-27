package io.mockative.kontinuity.configuration

import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.symbol.KSClassDeclaration
import io.mockative.kontinuity.Kontinuity
import io.mockative.kontinuity.KontinuityGeneration
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
        private fun fromAnnotation(
            annotation: Kontinuity?,
            parentConfiguration: SourceConfiguration
        ): ClassConfiguration {
            return ClassConfiguration(
                annotation?.wrapper?.ifEmpty { null } ?: parentConfiguration.wrappers,
                annotation?.generation?.ifUnspecified { parentConfiguration.generation }
                    ?: parentConfiguration.generation,
                annotation?.suspend?.ifEmpty { null } ?: parentConfiguration.suspend,
                annotation?.suspendFlow?.ifEmpty { null } ?: parentConfiguration.suspendFlow,
                annotation?.flow?.ifEmpty { null } ?: parentConfiguration.flow,
                annotation?.members?.ifEmpty { null } ?: parentConfiguration.members,
            )
        }

        fun fromDeclaration(
            declaration: KSClassDeclaration,
            parentConfiguration: SourceConfiguration
        ): ClassConfiguration {
            val annotation = declaration.getAnnotationsByType(Kontinuity::class).firstOrNull()
            return fromAnnotation(annotation, parentConfiguration)
        }
    }
}