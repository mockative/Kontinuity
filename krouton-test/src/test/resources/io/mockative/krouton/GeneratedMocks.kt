package io.mockative.krouton

internal fun <T> mock(@Suppress("UNUSED_PARAMETER") type: kotlin.reflect.KClass<io.mockative.krouton.PetStore<T>>): io.mockative.krouton.PetStore<T> where T : kotlin.CharSequence = io.mockative.krouton.PetStoreMock<T>()
internal fun mock(@Suppress("UNUSED_PARAMETER") type: kotlin.reflect.KClass<io.mockative.krouton.NoiseStore>): io.mockative.krouton.NoiseStore = io.mockative.krouton.NoiseStoreMock()
