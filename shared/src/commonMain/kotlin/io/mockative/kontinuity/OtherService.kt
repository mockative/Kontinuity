package io.mockative.kontinuity

import kotlinx.coroutines.flow.Flow

@Kontinuity
interface OtherService {
    var myFlow: Flow<Boolean>
}