package io.mockative.krouton.generator

import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.*

fun ClassName.toNativeClassName(): ClassName =
    ClassName(packageName, simpleNames.dropLast(1) + "Native${simpleNames.last()}")

fun ClassName.toNativeWrapperClassName(): ClassName =
    ClassName(packageName, simpleNames.dropLast(1) + "Native${simpleNames.last()}Wrapper")

fun KSPropertyDeclaration.getNativeName(type: ReturnType): String {
    return when (type) {
        is ReturnType.Value -> simpleName.asString()
        is ReturnType.Flow, is ReturnType.StateFlow -> "${simpleName.asString()}Native"
        else -> throw IllegalStateException("Unknown return type ${type::class}")
    }
}

fun KSFunctionDeclaration.getNativeName(type: FunctionType): String {
    return when (type) {
        is FunctionType.Blocking -> when (type.returnType) {
            is ReturnType.Value -> simpleName.asString()
            is ReturnType.Flow, is ReturnType.StateFlow -> "${simpleName.asString()}Native"
            else -> throw IllegalStateException("Unknown return type ${type.returnType::class}")
        }
        is FunctionType.Suspending -> "${simpleName.asString()}Native"
        else -> throw IllegalStateException("Unknown function type ${type::class}")
    }
}

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
        .mapNotNull { (it.declaration as? KSClassDeclaration) }
        .map { it.toClassName() }
        .map { it.toNativeClassName() }
        .toList()
}

sealed interface ReturnType {
    data class Value(val type: TypeName) : ReturnType
    data class Flow(val elementType: TypeName) : ReturnType
    data class StateFlow(val elementType: TypeName) : ReturnType
}

sealed interface FunctionType {
    data class Blocking(val returnType: ReturnType) : FunctionType
    data class Suspending(val returnType: ReturnType) : FunctionType
}

internal fun KSTypeReference.getReturnType(typeParameterResolver: TypeParameterResolver): ReturnType {
    val typeName = toTypeName(typeParameterResolver)

    if (typeName is ParameterizedTypeName) {
        when (typeName.rawType) {
            STATE_FLOW -> {
                return ReturnType.StateFlow(typeName.typeArguments[0])
            }
            FLOW, SHARED_FLOW -> {
                return ReturnType.Flow(typeName.typeArguments[0])
            }
        }
    }

    return ReturnType.Value(typeName)
}

internal fun KSFunctionDeclaration.getReturnType(typeParameterResolver: TypeParameterResolver): ReturnType {
    return returnType!!.getReturnType(typeParameterResolver)
}

internal fun KSFunctionDeclaration.getFunctionType(typeParameterResolver: TypeParameterResolver): FunctionType {
    val returnType = getReturnType(typeParameterResolver)

    return when {
        modifiers.contains(Modifier.SUSPEND) -> FunctionType.Suspending(returnType)
        else -> FunctionType.Blocking(returnType)
    }
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
                builder.returns(KONTINUITY_FLOW.parameterizedBy(returnType.elementType))
            else -> throw IllegalStateException("Unknown return type ${returnType::class}")
        }
        is FunctionType.Suspending -> when (val returnType = functionType.returnType) {
            is ReturnType.Value ->
                builder.returns(KONTINUITY_SUSPEND.parameterizedBy(returnType.type))
            is ReturnType.Flow ->
                builder.returns(KONTINUITY_SUSPEND.parameterizedBy(KONTINUITY_FLOW.parameterizedBy(returnType.elementType)))
            is ReturnType.StateFlow ->
                builder.returns(KONTINUITY_STATE_FLOW.parameterizedBy(returnType.elementType))
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

        is ReturnType.Flow -> PropertySpec.builder(name, KONTINUITY_FLOW.parameterizedBy(returnType.elementType))
            .build()

        is ReturnType.StateFlow -> PropertySpec.builder(name, KONTINUITY_STATE_FLOW.parameterizedBy(returnType.elementType))
            .build()

        else -> throw IllegalStateException("Unknown return type ${returnType::class}")
    }
}
