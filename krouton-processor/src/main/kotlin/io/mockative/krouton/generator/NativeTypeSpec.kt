package io.mockative.krouton.generator

import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.Modifier
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.*

fun ClassName.toNativeClassName(): ClassName =
    ClassName(packageName, simpleNames.dropLast(1) + "Native${simpleNames.last()}")

fun ClassName.toNativeWrapperClassName(): ClassName =
    ClassName(packageName, simpleNames.dropLast(1) + "Native${simpleNames.last()}Wrapper")

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
//    data class StateFlow(val elementType: TypeName) : ReturnType()
}

sealed interface FunctionType {
    data class Blocking(val returnType: ReturnType) : FunctionType
    data class Suspending(val returnType: ReturnType) : FunctionType
}

internal fun KSFunctionDeclaration.getReturnType(typeParameterResolver: TypeParameterResolver): ReturnType {
    val funDecReturnType = returnType!!.toTypeName(typeParameterResolver)
    val elementType = funDecReturnType.getFlowElementTypeNameOrNull()
    return elementType?.let { ReturnType.Flow(it) } ?: ReturnType.Value(funDecReturnType)
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
    val name = simpleName.asString()
    val functionType = getFunctionType(typeParameterResolver)

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

    return when (functionType) {
        is FunctionType.Blocking -> when (val returnType = functionType.returnType) {
            is ReturnType.Value -> builder
                .returns(returnType.type)
                .build()
            is ReturnType.Flow -> builder
                .returns(KONTINUITY_FLOW.parameterizedBy(returnType.elementType))
                .build()
        }
        is FunctionType.Suspending -> when (val returnType = functionType.returnType) {
            is ReturnType.Value -> builder
                .returns(KONTINUITY_SUSPEND.parameterizedBy(returnType.type))
                .build()
            is ReturnType.Flow -> builder
                .returns(KONTINUITY_SUSPEND.parameterizedBy(KONTINUITY_FLOW.parameterizedBy(returnType.elementType)))
                .build()
        }
    }
}

private fun KSClassDeclaration.buildNativePropertySpecs(typeParameterResolver: TypeParameterResolver): List<PropertySpec> {
    return getDeclaredProperties()
        .filter { it.isPublic() }
        .map { it.buildNativePropertySpec(typeParameterResolver) }
        .toList()
}

private fun KSPropertyDeclaration.buildNativePropertySpec(typeParameterResolver: TypeParameterResolver): PropertySpec {
    val name = simpleName.asString()

    val propertyDecType = type.toTypeName(typeParameterResolver)
    val elementType = propertyDecType.getFlowElementTypeNameOrNull()

    val modifiers = modifiers.mapNotNull { it.toKModifier() }

    return when (elementType) {
        null -> PropertySpec.builder(name, propertyDecType, modifiers)
            .mutable(isMutable)
            .build()

        else -> PropertySpec.builder(name, KONTINUITY_FLOW.parameterizedBy(elementType), modifiers)
            .build()
    }
}

fun TypeName.getFlowElementTypeNameOrNull(): TypeName? {
    if (this !is ParameterizedTypeName) {
        return null
    }

    if (!rawType.isFlow) {
        return null
    }

    return typeArguments[0]
}
