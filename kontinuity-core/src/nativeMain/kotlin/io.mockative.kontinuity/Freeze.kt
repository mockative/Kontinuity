package io.mockative.kontinuity

import kotlin.native.concurrent.freeze

actual fun <T> T.freeze(): T = this.freeze()
