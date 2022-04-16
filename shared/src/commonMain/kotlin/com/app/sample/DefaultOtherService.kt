package com.app.sample

import kotlinx.coroutines.flow.Flow

class DefaultOtherService : OtherService {
    override var myFlow: Flow<Boolean>
        get() = TODO("Not yet implemented")
        set(value) {}

}