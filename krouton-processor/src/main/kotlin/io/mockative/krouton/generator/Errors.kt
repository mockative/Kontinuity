package io.mockative.krouton.generator

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.ksp.toClassName

internal object Errors {
    fun functionParameterMissingName(
        classDec: KSClassDeclaration,
        function: KSFunctionDeclaration,
        index: Int
    ) = IllegalStateException(
        "Krouton function parameters must all have names, but the parameter at index $index of " +
                "the function `${function.simpleName.asString()}` in the type " +
                "`${classDec.toClassName()}` does not have a name. ${function.location}"
    )

    fun functionReturnTypeCouldNotBeResolved(
        classDec: KSClassDeclaration,
        function: KSFunctionDeclaration
    ) = IllegalStateException(
        "The return type of the function `${function.simpleName.asString()}` in type " +
                "`${classDec.toClassName()}` could not be resolved. ${function.location}"
    )
}