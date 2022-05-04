package io.mockative.kontinuity.viewmodel

import android.os.Looper
import androidx.arch.core.executor.ArchTaskExecutor
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlin.reflect.KProperty

actual typealias MutableState<T> = MutableLiveData<T>

@Suppress("UNCHECKED_CAST")
actual operator fun <T> MutableState<T>.getValue(thisRef: Any?, property: KProperty<*>): T {
    return value as T
}

actual operator fun <T> MutableState<T>.setValue(thisRef: Any?, property: KProperty<*>, value: T) {
    if (Looper.getMainLooper() == Looper.myLooper()) {
        this.value = value
    } else {
        postValue(value)
    }
}

actual abstract class PlatformViewModel : ViewModel() {
    val objectWillChange

    actual fun <T> mutableStateOf(value: T): MutableState<T> {
        return MutableState(value)
    }
}
