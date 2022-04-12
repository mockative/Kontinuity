package io.mockative.kontinuity

/**
 * Freezes this object in Kotlin/Native.
 */
internal expect fun <T> T.freeze(): T
