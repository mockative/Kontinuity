package io.mockative.kontinuity.configuration

import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import io.mockative.kontinuity.KONTINUITY_CONFIGURATION_ANNOTATION
import io.mockative.kontinuity.KontinuityConfiguration
import io.mockative.kontinuity.KontinuityGeneration
import io.mockative.kontinuity.ksp.ifUnspecified

data class SourceConfiguration(
    val wrappers: String,
    val generation: KontinuityGeneration,
    val suspend: String,
    val flow: String,
    val suspendFlow: String,
    val members: String,
) {
    companion object {
        private fun fromAnnotation(
            annotation: KontinuityConfiguration?,
            parentConfiguration: KSPArgumentConfiguration
        ): SourceConfiguration {
            return SourceConfiguration(
                annotation?.wrappers?.ifEmpty { null } ?: parentConfiguration.wrappers,
                annotation?.generation?.ifUnspecified { parentConfiguration.generation }
                    ?: parentConfiguration.generation,
                annotation?.suspend?.ifEmpty { null } ?: parentConfiguration.suspend,
                annotation?.suspendFlow?.ifEmpty { null } ?: parentConfiguration.suspendFlow,
                annotation?.flow?.ifEmpty { null } ?: parentConfiguration.flow,
                annotation?.members?.ifEmpty { null } ?: parentConfiguration.members,
            )
        }

        fun fromResolver(resolver: Resolver, log: KSPLogger, parentConfiguration: KSPArgumentConfiguration): SourceConfiguration? {
            val sourceConfigurationClasses = resolver
                .getSymbolsWithAnnotation(KONTINUITY_CONFIGURATION_ANNOTATION.canonicalName)
                .toList()

            if (sourceConfigurationClasses.size > 1) {
                sourceConfigurationClasses.forEach {
                    log.error("@KontinuityConfiguration was found on multiple classes. Only one configuration attribute per source set is allowed.", it)
                }

                return null
            }

            val sourceConfigurationClassAnnotation = sourceConfigurationClasses
                .flatMap { it.getAnnotationsByType(KontinuityConfiguration::class) }
                .firstOrNull()

            return fromAnnotation(sourceConfigurationClassAnnotation, parentConfiguration)
        }
    }
}