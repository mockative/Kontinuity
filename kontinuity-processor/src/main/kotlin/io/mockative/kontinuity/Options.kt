package io.mockative.kontinuity

import java.util.*

object Options {
    lateinit var source: Map<String, String>

    object Logging {
        val level = source["kontinuity.logging.level"] ?: "WARN"
    }

    object Generator {
        // K%s
        val interfaceName = source["kontinuity.generator.interfaceName"] ?: "K%s"

        // K%sWrapper
        val wrapperClassName = source["kontinuity.generator.wrapperClassName"] ?: "K%sWrapper"

        // %K
        val transformedMemberName = source["kontinuity.generator.transformedMemberName"] ?: "%sK"

        fun getTransformedMemberName(member: String): String =
            transformedMemberName.getMemberName(member)

        // %s
        val simpleMemberName = source["kontinuity.generator.simpleMemberName"] ?: "%s"

        fun getSimpleMemberName(member: String): String =
            simpleMemberName.getMemberName(member)

        private fun String.getMemberName(name: String): String =
            format(when {
                this == "%s" -> name
                endsWith("%s") -> replaceFirstChar { it.titlecase(Locale.getDefault()) }
                else -> name
            })
    }

    override fun toString(): String {
        return """
            |kontinuity.logging.level: "${Logging.level}"
            |kontinuity.generator.interfaceName: "${Generator.interfaceName}"
            |kontinuity.generator.wrapperClassName: "${Generator.wrapperClassName}"
            |kontinuity.generator.transformedMemberName: "${Generator.transformedMemberName}"
            |kontinuity.generator.simpleMemberName: "${Generator.simpleMemberName}"
        """.trimMargin()
    }
}
