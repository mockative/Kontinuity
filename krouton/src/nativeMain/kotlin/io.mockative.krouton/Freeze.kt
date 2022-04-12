package io.mockative.krouton

import kotlin.native.concurrent.freeze

actual fun <T> T.freeze(): T = this.freeze()
