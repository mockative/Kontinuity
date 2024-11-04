package io.mockative.kontinuity.configuration

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotation
import io.mockative.kontinuity.KONTINUITY_CONFIGURATION_ANNOTATION
import io.mockative.kontinuity.KontinuityConfiguration
import io.mockative.kontinuity.KontinuityGeneration
import io.mockative.kontinuity.getAnnotationsByClassName
import io.mockative.kontinuity.getValue
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
        private val defaults = KontinuityConfiguration()

        private fun fromAnnotation(
            annotation: KSAnnotation?,
            parentConfiguration: KSPArgumentConfiguration
        ): SourceConfiguration {
            return SourceConfiguration(
                annotation?.getValue("wrappers", defaults.wrappers)
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
                .flatMap { it.getAnnotationsByClassName(KONTINUITY_CONFIGURATION_ANNOTATION) }
                .firstOrNull()

            return fromAnnotation(sourceConfigurationClassAnnotation, parentConfiguration)
        }
    }
}
