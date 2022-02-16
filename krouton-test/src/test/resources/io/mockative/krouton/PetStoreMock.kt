package io.mockative.krouton

class PetStoreMock<T> : io.mockative.krouton.Mockable(stubsUnitByDefault = true), io.mockative.krouton.PetStore<T> where T : kotlin.CharSequence {
    override var pets: kotlin.collections.Map<kotlin.String, kotlin.Function0<kotlin.Unit>>
        get() = io.mockative.krouton.Mockable.invoke(this, io.mockative.krouton.Invocation.Getter("pets"), false)
        set(value) = io.mockative.krouton.Mockable.invoke(this, io.mockative.krouton.Invocation.Setter("pets", value), true)
    override val readOnlyPets: kotlin.collections.Map<kotlin.String, kotlin.Function0<kotlin.Unit>>
        get() = io.mockative.krouton.Mockable.invoke(this, io.mockative.krouton.Invocation.Getter("readOnlyPets"), false)

    override fun add(pet: io.mockative.krouton.Pet): kotlin.Unit = io.mockative.krouton.Mockable.invoke<kotlin.Unit>(this, io.mockative.krouton.Invocation.Function("add", listOf<Any?>(pet)), true)
    override fun <R> call(function: kotlin.Function1<kotlin.Any, R>): R where R : kotlin.Any? = io.mockative.krouton.Mockable.invoke<R>(this, io.mockative.krouton.Invocation.Function("call", listOf<Any?>(function)), false)
    override fun clear(): kotlin.Unit = io.mockative.krouton.Mockable.invoke<kotlin.Unit>(this, io.mockative.krouton.Invocation.Function("clear", emptyList<Any?>()), true)
    override fun <P> generic(type: T, pet: P): kotlin.CharSequence where P : kotlin.Number = io.mockative.krouton.Mockable.invoke<kotlin.CharSequence>(this, io.mockative.krouton.Invocation.Function("generic", listOf<Any?>(type, pet)), false)
    override fun pet(name: kotlin.String): io.mockative.krouton.Pet = io.mockative.krouton.Mockable.invoke<io.mockative.krouton.Pet>(this, io.mockative.krouton.Invocation.Function("pet", listOf<Any?>(name)), false)
    override fun petOrNull(name: kotlin.String): io.mockative.krouton.Pet? = io.mockative.krouton.Mockable.invoke<io.mockative.krouton.Pet?>(this, io.mockative.krouton.Invocation.Function("petOrNull", listOf<Any?>(name)), false)
}