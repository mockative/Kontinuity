package com.app.sample

import io.mockative.kontinuity.Kontinuity
import kotlinx.coroutines.flow.Flow

@Kontinuity
interface OtherService {
    var myFlow: Flow<Boolean>
}