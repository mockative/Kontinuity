package io.mockative.krouton

import kotlinx.coroutines.Job

typealias KontinuityCancellable = () -> Unit

@Suppress("NOTHING_TO_INLINE")
internal inline fun Job.asNativeCancellable(): KontinuityCancellable = { cancel() }.freeze()