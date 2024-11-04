package com.app.sample.kontinuity

import io.mockative.kontinuity.KontinuityScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

@KontinuityScope(default = true)
internal val defaultScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
