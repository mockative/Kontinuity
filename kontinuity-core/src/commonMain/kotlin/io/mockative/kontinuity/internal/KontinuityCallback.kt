package io.mockative.kontinuity.internal

typealias KontinuityCallback<T> = (T, Unit) -> Unit

@Suppress("NOTHING_TO_INLINE")
internal inline operator fun <T> KontinuityCallback<T>.invoke(value: T) = invoke(value, Unit)