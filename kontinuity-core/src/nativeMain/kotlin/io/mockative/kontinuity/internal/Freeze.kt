package io.mockative.kontinuity.internal

import kotlin.native.concurrent.freeze

actual fun <T> T.freeze(): T = this.freeze()
