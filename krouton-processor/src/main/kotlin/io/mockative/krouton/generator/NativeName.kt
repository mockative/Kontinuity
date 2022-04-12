package io.mockative.krouton.generator

import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import io.mockative.krouton.FunctionType
import io.mockative.krouton.Options
import io.mockative.krouton.ReturnType

fun KSPropertyDeclaration.getNativeName(type: ReturnType): String {
    return when (type) {
        is ReturnType.Value ->
            simpleName.asString()

        is ReturnType.Flow, is ReturnType.StateFlow -> {
            val memberName = Options.Generator.getMemberName(simpleName.asString())
            memberName.format(simpleName.asString())
        }

        else -> throw IllegalStateException("Unknown return type ${type::class}")
    }
}

fun KSFunctionDeclaration.getNativeName(type: FunctionType): String {
    return when (type) {
        is FunctionType.Blocking -> when (type.returnType) {
            is ReturnType.Value ->
                simpleName.asString()

            is ReturnType.Flow, is ReturnType.StateFlow ->
                Options.Generator.getMemberName(simpleName.asString())

            else -> throw IllegalStateException("Unknown return type ${type.returnType::class}")
        }

        is FunctionType.Suspending -> Options.Generator.getMemberName(simpleName.asString())

        else -> throw IllegalStateException("Unknown function type ${type::class}")
    }
}
