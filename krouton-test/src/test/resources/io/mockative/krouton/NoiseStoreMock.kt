package io.mockative.krouton

class NoiseStoreMock : io.mockative.krouton.Mockable(stubsUnitByDefault = true), io.mockative.krouton.NoiseStore {
    override var noises: kotlin.collections.Map<kotlin.String, kotlin.Function0<kotlin.Unit>>
        get() = io.mockative.krouton.Mockable.invoke(this, io.mockative.krouton.Invocation.Getter("noises"), false)
        set(value) = io.mockative.krouton.Mockable.invoke(this, io.mockative.krouton.Invocation.Setter("noises", value), true)
    override val readOnlyNoises: kotlin.collections.Map<kotlin.String, kotlin.Function0<kotlin.Unit>>
        get() = io.mockative.krouton.Mockable.invoke(this, io.mockative.krouton.Invocation.Getter("readOnlyNoises"), false)

    override fun addNoise(name: kotlin.String, play: kotlin.Function0<kotlin.Unit>): kotlin.Unit = io.mockative.krouton.Mockable.invoke<kotlin.Unit>(this, io.mockative.krouton.Invocation.Function("addNoise", listOf<Any?>(name, play)), true)
    override fun clear(): kotlin.Unit = io.mockative.krouton.Mockable.invoke<kotlin.Unit>(this, io.mockative.krouton.Invocation.Function("clear", emptyList<Any?>()), true)
    override fun noise(name: kotlin.String): kotlin.Function0<kotlin.Unit> = io.mockative.krouton.Mockable.invoke<kotlin.Function0<kotlin.Unit>>(this, io.mockative.krouton.Invocation.Function("noise", listOf<Any?>(name)), false)
}