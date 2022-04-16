package io.mockative.kontinuity.koin

internal object Options {
    lateinit var source: Map<String, String>

    object Logging {
        val level = source["kontinuity-koin.logging.level"] ?: "WARN"
    }

    override fun toString(): String {
        return """
            |kontinuity-koin.logging.level: "${Logging.level}"
        """.trimMargin()
    }
}
