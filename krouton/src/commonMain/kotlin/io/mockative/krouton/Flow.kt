package io.mockative.krouton

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

fun <T> collectFlow(flow: Flow<T>, onElement: (T) -> Unit, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit): Cancellation {
    val mainScope = CoroutineScope(Dispatchers.Main.immediate)

    val job = mainScope.launch {
        try {
            flow.collect(onElement)
            onSuccess()
        } catch (error: Throwable) {
            onFailure(error)
        }
    }

    return { job.cancel() }
}
