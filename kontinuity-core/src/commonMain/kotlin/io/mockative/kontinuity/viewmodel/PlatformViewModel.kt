package io.mockative.kontinuity.viewmodel

import kotlin.reflect.KProperty

expect class MutableState<T>

expect operator fun <T> MutableState<T>.getValue(thisRef: Any?, property: KProperty<*>): T
expect operator fun <T> MutableState<T>.setValue(thisRef: Any?, property: KProperty<*>, value: T)

expect abstract class PlatformViewModel() {
    fun <T> mutableStateOf(value: T): MutableState<T>
}