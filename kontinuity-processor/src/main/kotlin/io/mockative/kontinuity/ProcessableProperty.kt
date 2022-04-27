package io.mockative.kontinuity

import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.ksp.TypeParameterResolver
import com.squareup.kotlinpoet.ksp.toTypeParameterResolver

data class ProcessableProperty(
    val declaration: KSPropertyDeclaration,
    val sourceMemberName: String,
    val wrapperMemberName: String,
    val type: ReturnType,
    val isMutable: Boolean,
) {
    companion object {
        fun fromDeclaration(
            declaration: KSPropertyDeclaration,
            parentConfiguration: ClassConfiguration,
            parentTypeParameterResolver: TypeParameterResolver
        ): ProcessableProperty? {
            val typeParameterResolver = declaration.typeParameters
                .toTypeParameterResolver(parentTypeParameterResolver)

            val returnType = ReturnType.fromDeclaration(declaration, typeParameterResolver)

            val configuration =
                MemberConfiguration.fromDeclaration(declaration, returnType, parentConfiguration)

            if (!configuration.generate) {
                return null
            }

            return with(declaration) {
                val simpleName = declaration.simpleName.asString()

                val sourceMemberName = simpleName
                val wrapperMemberName = configuration.name.replace("%M", simpleName)

                ProcessableProperty(
                    declaration,
                    sourceMemberName,
                    wrapperMemberName,
                    returnType,
                    declaration.isMutable
                )
            }
        }
    }
}