package io.mockative.kontinuity

import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.ksp.TypeParameterResolver
import com.squareup.kotlinpoet.ksp.toTypeParameterResolver
import io.mockative.kontinuity.configuration.ClassConfiguration
import io.mockative.kontinuity.configuration.MemberConfiguration
import io.mockative.kontinuity.ksp.isFromAny

data class ProcessableFunction(
    val declaration: KSFunctionDeclaration,
    val sourceMemberName: String,
    val wrapperMemberName: String,
    val functionType: FunctionType,
    val override: Boolean,
    val typeParameterResolver: TypeParameterResolver,
    val scopeDeclaration: KSPropertyDeclaration?,
) {
    companion object {
        fun fromDeclaration(
            declaration: KSFunctionDeclaration,
            parentConfiguration: ClassConfiguration,
            parentTypeParameterResolver: TypeParameterResolver,
            scopeDeclaration: KSPropertyDeclaration?,
        ): ProcessableFunction? {
            val typeParameterResolver = declaration.typeParameters
                .toTypeParameterResolver(parentTypeParameterResolver)

            val functionType = FunctionType.fromDeclaration(declaration, typeParameterResolver)

            val configuration =
                MemberConfiguration.fromDeclaration(declaration, functionType, parentConfiguration)

            if (!configuration.generate) {
                return null
            }

            return with(declaration) {
                val simpleName = declaration.simpleName.asString()

                val sourceMemberName = simpleName
                val wrapperMemberName = configuration.name.replace("%M", simpleName)
                val override = isFromAny()

                ProcessableFunction(
                    declaration,
                    sourceMemberName,
                    wrapperMemberName,
                    functionType,
                    override,
                    typeParameterResolver,
                    scopeDeclaration,
                )
            }
        }
    }
}