package io.mockative.krouton

typealias KontinuityCallback<T> = (T, Unit) -> Unit

@Suppress("NOTHING_TO_INLINE")
internal inline operator fun <T> KontinuityCallback<T>.invoke(value: T) = invoke(value.freeze(), Unit)