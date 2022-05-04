package io.mockative.kontinuity.viewmodel

import kotlinx.atomicfu.atomic
import kotlin.reflect.KProperty

class Subscription(private val cancel: () -> Unit) {
    fun cancel() {
        cancel.invoke()
    }
}

typealias ViewModelEventListener = (Unit) -> Unit

class ViewModelEvent {
    private var listeners: List<ViewModelEventListener> by atomic(emptyList())

    fun addListener(listener: ViewModelEventListener): Subscription {
        listeners = listeners + listener
        return Subscription { listeners = listeners - listener }
    }

    fun send() {
        listeners.forEach { listener -> listener(Unit) }
    }
}

actual class MutableState<T>(
    value: T,
    private val objectWillChange: ViewModelEvent,
    private val objectDidChange: ViewModelEvent
) {
    private var _value: T = value

    var value: T
        get() = _value
        set(value) {
            objectWillChange.send()
            _value = value
            objectDidChange.send()
        }
}

actual operator fun <T> MutableState<T>.getValue(thisRef: Any?, property: KProperty<*>): T {
    return value
}

actual operator fun <T> MutableState<T>.setValue(thisRef: Any?, property: KProperty<*>, value: T) {
    this.value = value
}

actual abstract class PlatformViewModel {
    val objectWillChange = ViewModelEvent()
    val objectDidChange = ViewModelEvent()

    actual fun <T> mutableStateOf(value: T): MutableState<T> {
        return MutableState(value, objectWillChange, objectDidChange)
    }
}