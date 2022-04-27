package io.mockative.kontinuity

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSNode

class PrefixedKSPLogger(private val log: KSPLogger) : KSPLogger {
    override fun error(message: String, symbol: KSNode?) {
        log.error("[Kontinuity] $message", symbol)
    }

    override fun exception(e: Throwable) {
        log.exception(e)
    }

    override fun info(message: String, symbol: KSNode?) {
        log.info("[Kontinuity] $message", symbol)
    }

    override fun logging(message: String, symbol: KSNode?) {
        log.logging("[Kontinuity] $message", symbol)
    }

    override fun warn(message: String, symbol: KSNode?) {
        log.warn("[Kontinuity] $message", symbol)
    }
}