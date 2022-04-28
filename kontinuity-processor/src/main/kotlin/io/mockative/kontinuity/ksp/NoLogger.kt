package io.mockative.kontinuity.ksp

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSNode

class NoLogger : KSPLogger {
    override fun error(message: String, symbol: KSNode?) {
        // Nothing
    }

    override fun exception(e: Throwable) {
        // Nothing
    }

    override fun info(message: String, symbol: KSNode?) {
        // Nothing
    }

    override fun logging(message: String, symbol: KSNode?) {
        // Nothing
    }

    override fun warn(message: String, symbol: KSNode?) {
        // Nothing
    }
}
