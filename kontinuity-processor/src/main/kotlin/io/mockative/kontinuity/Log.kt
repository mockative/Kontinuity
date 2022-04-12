package io.mockative.kontinuity

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSNode

object Log {
    lateinit var logger: KSPLogger

    private const val format = "[Kontinuity] %s"

    private val level: String
        get() = Options.Logging.level

    private val logDebug: Boolean
        get() = level.equals("DEBUG", true)

    private val logInfo: Boolean
        get() = logDebug || level.equals("INFO", true)

    fun debug(message: String, symbol: KSNode? = null) {
        if (logDebug) {
            logger.info(format.format(message), symbol)
        }
    }

    fun info(message: String, symbol: KSNode? = null) {
        if (logInfo) {
            logger.info(format.format(message), symbol)
        }
    }

    fun warn(message: String, symbol: KSNode? = null) =
        logger.warn(format.format(message), symbol)

    fun error(message: String, symbol: KSNode? = null) =
        logger.error(format.format(message), symbol)

    fun exception(e: Throwable) =
        logger.exception(e)
}
