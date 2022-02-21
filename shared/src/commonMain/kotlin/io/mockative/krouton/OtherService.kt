package io.mockative.krouton

import kotlinx.coroutines.flow.Flow

@Krouton
interface OtherService {
    var myFlow: Flow<Boolean>
}