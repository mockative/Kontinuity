package io.mockative.kontinuity

import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.ksp.TypeParameterResolver
import com.squareup.kotlinpoet.ksp.toTypeParameterResolver

data class ProcessableFunction(
    val declaration: KSFunctionDeclaration,
    val sourceMemberName: String,
    val wrapperMemberName: String,
    val functionType: FunctionType,
    val override: Boolean,
    val typeParameterResolver: TypeParameterResolver
) {
    companion object {
        fun fromDeclaration(
            declaration: KSFunctionDeclaration,
            parentConfiguration: ClassConfiguration,
            parentTypeParameterResolver: TypeParameterResolver
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
                )
            }
        }
    }
}