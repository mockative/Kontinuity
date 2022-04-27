package io.mockative.kontinuity

object Options {
    lateinit var source: Map<String, String>

    object Logging {
        val level = source["kontinuity.logging.level"] ?: "WARN"
    }

    override fun toString(): String {
        return """
            |kontinuity.logging.level: "${Logging.level}"
        """.trimMargin()
    }
}
