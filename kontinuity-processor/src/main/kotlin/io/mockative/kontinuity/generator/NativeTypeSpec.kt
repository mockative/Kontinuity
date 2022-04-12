package io.mockative.kontinuity.generator

import com.google.devtools.ksp.*
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.*
import io.mockative.kontinuity.FunctionType
import io.mockative.kontinuity.ReturnType
import io.mockative.kontinuity.getFunctionType
import io.mockative.kontinuity.getReturnType

fun KSClassDeclaration.buildNativeTypeSpec(className: ClassName): TypeSpec {
    val typeParameterResolver = typeParameters.toTypeParameterResolver()

    return TypeSpec.interfaceBuilder(className)
        .addSuperinterfaces(buildNativeSuperinterfaces())
        .addProperties(buildNativePropertySpecs(typeParameterResolver))
        .addFunctions(buildNativeFunSpecs(typeParameterResolver))
        .addKdoc(docString?.trim() ?: "")
        .build()
}

private fun KSClassDeclaration.buildNativeSuperinterfaces(): List<ClassName> {
    return getAllSuperTypes()
        .mapNotNull { (it.declaration as? KSClassDeclaration)?.toNativeInterfaceClassName() }
        .toList()
}

private fun KSClassDeclaration.buildNativeFunSpecs(typeParameterResolver: TypeParameterResolver): List<FunSpec> {
    return getDeclaredFunctions()
        .filter { it.isPublic() }
        .map { it.buildNativeFunSpec(typeParameterResolver) }
        .toList()
}

private fun KSFunctionDeclaration.buildNativeFunSpec(typeParameterResolver: TypeParameterResolver): FunSpec {
    val functionType = getFunctionType(typeParameterResolver)
    val name = getNativeName(functionType)

    val builder = FunSpec.builder(name)
        .addModifiers(KModifier.ABSTRACT)
        .addTypeVariables(typeParameters.map { it.toTypeVariableName(typeParameterResolver) })
        .addParameters(parameters.map {
            ParameterSpec
                .builder(
                    it.name!!.asString(),
                    it.type.toTypeName(typeParameterResolver),
                    it.modifiers
                )
                .build()
        })

    when (functionType) {
        is FunctionType.Blocking -> when (val returnType = functionType.returnType) {
            is ReturnType.Value ->
                builder.returns(returnType.type)
            is ReturnType.Flow ->
                builder.returns(KONTINUITY_FLOW.parameterizedBy(returnType.elementType))
            is ReturnType.StateFlow ->
                builder.returns(KONTINUITY_STATE_FLOW.parameterizedBy(returnType.elementType))
            else -> throw IllegalStateException("Unknown return type ${returnType::class}")
        }
        is FunctionType.Suspending -> when (val returnType = functionType.returnType) {
            is ReturnType.Value ->
                builder.returns(KONTINUITY_SUSPEND.parameterizedBy(returnType.type))
            is ReturnType.Flow ->
                builder.returns(
                    KONTINUITY_SUSPEND.parameterizedBy(
                        KONTINUITY_FLOW.parameterizedBy(returnType.elementType)
                    )
                )
            is ReturnType.StateFlow ->
                builder.returns(
                    KONTINUITY_SUSPEND.parameterizedBy(
                        KONTINUITY_STATE_FLOW.parameterizedBy(returnType.elementType)
                    )
                )
            else -> throw IllegalStateException("Unknown return type ${returnType::class}")
        }
        else -> throw IllegalStateException("Unknown function type ${functionType::class}")
    }

    return builder.build()
}

private fun KSClassDeclaration.buildNativePropertySpecs(typeParameterResolver: TypeParameterResolver): List<PropertySpec> {
    return getDeclaredProperties()
        .filter { it.isPublic() }
        .map { it.buildNativePropertySpec(typeParameterResolver) }
        .toList()
}

private fun KSPropertyDeclaration.buildNativePropertySpec(typeParameterResolver: TypeParameterResolver): PropertySpec {
    val returnType = type.getReturnType(typeParameterResolver)
    val name = getNativeName(returnType)
    return when (returnType) {
        is ReturnType.Value -> PropertySpec.builder(name, returnType.type)
            .mutable(isMutable)
            .build()

        is ReturnType.Flow -> {
            val propertyType = KONTINUITY_FLOW.parameterizedBy(returnType.elementType)

            PropertySpec.builder(name, propertyType)
                .build()
        }

        is ReturnType.StateFlow -> {
            val propertyType = KONTINUITY_STATE_FLOW.parameterizedBy(returnType.elementType)

            PropertySpec.builder(name, propertyType)
                .build()
        }

        else -> throw IllegalStateException("Unknown return type ${returnType::class}")
    }
}
