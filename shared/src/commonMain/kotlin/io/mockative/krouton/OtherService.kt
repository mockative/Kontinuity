package io.mockative.krouton

import kotlinx.coroutines.flow.Flow

@Kontinuity
interface OtherService {
    var myFlow: Flow<Boolean>
}