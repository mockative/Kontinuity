package io.mockative.kontinuity.configuration

import io.mockative.kontinuity.KontinuityGeneration
import io.mockative.kontinuity.ksp.ifUnspecified

data class KSPArgumentConfiguration(
    val wrappers: String,
    val generation: KontinuityGeneration,
    val suspend: String,
    val suspendFlow: String,
    val flow: String,
    val members: String,
) {
    companion object {
        fun fromOptions(
            options: Map<String, String>,
            parentConfiguration: DefaultConfiguration
        ): KSPArgumentConfiguration {
            return with(options) {
                KSPArgumentConfiguration(
                    wrappers = get("kontinuity.configuration.wrappers")
                        ?: parentConfiguration.wrappers,
                    generation = get("kontinuity.configuration.generation")
                        ?.let { KontinuityGeneration.valueOf(it) }
                        ?.ifUnspecified { parentConfiguration.generation }
                        ?: parentConfiguration.generation,
                    suspend = get("kontinuity.configuration.suspend")
                        ?: parentConfiguration.suspend,
                    suspendFlow = get("kontinuity.configuration.suspendFlow")
                        ?: parentConfiguration.suspendFlow,
                    flow = get("kontinuity.configuration.flow")
                        ?: parentConfiguration.flow,
                    members = get("kontinuity.configuration.members")
                        ?: parentConfiguration.members,
                )
            }
        }
    }
}