package io.mockative.kontinuity

data class SourceConfiguration(
    val wrappers: String,
    val generation: KontinuityGeneration,
    val suspend: String,
    val flow: String,
    val suspendFlow: String,
    val members: String,
) {
    companion object {
        fun fromAnnotation(
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
    }
}