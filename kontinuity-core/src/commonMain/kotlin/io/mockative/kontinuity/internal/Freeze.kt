package io.mockative.kontinuity.internal

/**
 * Freezes this object in Kotlin/Native.
 */
internal expect fun <T> T.freeze(): T
