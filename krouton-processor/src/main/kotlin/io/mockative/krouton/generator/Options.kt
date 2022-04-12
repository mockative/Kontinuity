package io.mockative.krouton.generator

import java.util.*

object Options {
    lateinit var source: Map<String, String>

    object Logging {
        val level = source["kontinuity.logging.level"] ?: "WARN"
    }

    object Generator {
        // Native%s
        val interfaceName = source["kontinuity.generator.interfaceName"] ?: "Kontinuity%s"

        // Native%sWrapper
        val wrapperClassName = source["kontinuity.generator.wrapperClassName"] ?: "Kontinuity%sWrapper"

        // %sNative
        val memberName = source["kontinuity.generator.memberName"] ?: "%sNative"

        fun getMemberName(name: String): String {
            val member = if (memberName.endsWith("%s")) {
                name.replaceFirstChar { it.titlecase(Locale.getDefault()) }
            } else {
                name
            }

            return memberName.format(member)
        }
    }

    override fun toString(): String {
        return """
            |kontinuity.logging.level: "${Logging.level}"
            |kontinuity.generator.interfaceName: "${Generator.interfaceName}"
            |kontinuity.generator.wrapperClassName: "${Generator.wrapperClassName}"
            |kontinuity.generator.memberName: "${Generator.memberName}"
        """.trimMargin()
    }
}
