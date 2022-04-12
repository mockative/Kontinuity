package io.mockative.krouton

/**
 * Freezes this object in Kotlin/Native.
 */
internal expect fun <T> T.freeze(): T
