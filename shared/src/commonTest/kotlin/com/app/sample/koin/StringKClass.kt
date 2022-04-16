package com.app.sample.koin

import kotlin.reflect.KClass

data class StringKClass(
    override val qualifiedName: String,
    override val simpleName: String,
) : KClass<Any> {
    override fun isInstance(value: Any?): Boolean {
        return value != null && value::class.qualifiedName == qualifiedName
    }
}
