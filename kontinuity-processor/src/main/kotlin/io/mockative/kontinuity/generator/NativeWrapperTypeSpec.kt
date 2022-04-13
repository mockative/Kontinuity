package io.mockative.kontinuity.generator

import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.*
import io.mockative.kontinuity.*
import io.mockative.kontinuity.getFunctionType
import io.mockative.kontinuity.getReturnType

fun KSClassDeclaration.buildNativeWrapperTypeSpec(className: ClassName): TypeSpec {
    val typeParameterResolver = typeParameters.toTypeParameterResolver()

    val wrappedPropertySpec = buildWrappedPropertySpec()

    return TypeSpec.classBuilder(className)
        .addModifiers(KModifier.OPEN)
        .addProperty(wrappedPropertySpec)
        .addFunction(buildEmptyConstructorSpec())
        .addFunction(buildWrappedConstructorSpec(wrappedPropertySpec))
        .addProperties(buildNativePropertySpecs(typeParameterResolver))
        .addFunctions(buildNativeFunSpecs(typeParameterResolver))
        .addKdoc(docString?.trim() ?: "")
        .build()
}

private fun KSClassDeclaration.buildWrappedPropertySpec() =
    PropertySpec.builder("wrapped", toClassName(), KModifier.PRIVATE, KModifier.LATEINIT)
        .mutable(true)
        .build()

private fun KSClassDeclaration.buildWrappedConstructorSpec(propertySpec: PropertySpec): FunSpec {
    val parameter = ParameterSpec.builder("wrapping", toClassName())
        .build()

    return FunSpec.constructorBuilder()
        .addParameter(parameter)
        .addStatement("%N = %N", propertySpec, parameter)
        .build()
}

private fun KSClassDeclaration.buildEmptyConstructorSpec(): FunSpec {
    return FunSpec.constructorBuilder()
        .build()
}

private fun KSClassDeclaration.buildNativeFunSpecs(typeParameterResolver: TypeParameterResolver): List<FunSpec> {
    return getAllFunctions()
        .filter { it.isPublic() }
        .map { it.buildNativeFunSpec(typeParameterResolver) }
        .toList()
}

private fun KSFunctionDeclaration.getModifiers(): List<KModifier> =
    when {
        isFromAny() -> listOf(KModifier.OVERRIDE)
        else -> emptyList()
    }

private fun KSFunctionDeclaration.buildNativeFunSpec(typeParameterResolver: TypeParameterResolver): FunSpec {
    val name = simpleName.asString()

    val functionType = getFunctionType(typeParameterResolver)
    val nativeName = getNativeName(functionType)
    val modifiers = getModifiers()

    val builder = FunSpec.builder(nativeName)
        .addModifiers(modifiers)
        .addTypeVariables(buildNativeTypeParameterSpecs(typeParameterResolver))
        .addParameters(buildNativeParameterSpecs(typeParameterResolver))
        .addAnnotations(buildNativeThrowsAnnotationSpecs())

    val arguments = parameters.joinToString(", ") { parameter -> parameter.name!!.asString() }

    return when (functionType) {
        is FunctionType.Blocking -> when (val returnType = functionType.returnType) {
            is ReturnType.Value -> builder
                .returns(returnType.type)
                .addStatement("return wrapped.$name($arguments)")
                .build()
            is ReturnType.Flow -> builder
                .implementsFlow(returnType.elementType, name, arguments)
                .build()
            is ReturnType.StateFlow -> builder
                .implementsStateFlow(returnType.elementType, name, arguments)
                .build()
            else -> throw IllegalStateException("Unknown return type ${returnType::class}")
        }
        is FunctionType.Suspending -> when (val returnType = functionType.returnType) {
            is ReturnType.Value -> builder
                .implementsSuspend(returnType.type, name, arguments)
                .build()
            is ReturnType.Flow -> builder
                .implementsSuspendFlow(returnType.elementType, name, arguments)
                .build()
            is ReturnType.StateFlow -> builder
                .implementsSuspendStateFlow(returnType.elementType, name, arguments)
                .build()
            else -> throw IllegalStateException("Unknown return type ${returnType::class}")
        }
        else -> throw IllegalStateException("Unknown function type ${functionType::class}")
    }
}

private fun KSFunctionDeclaration.buildNativeTypeParameterSpecs(
    typeParameterResolver: TypeParameterResolver
) = typeParameters.map { it.toTypeVariableName(typeParameterResolver) }

private fun KSFunctionDeclaration.buildNativeParameterSpecs(
    typeParameterResolver: TypeParameterResolver
) = parameters.map { it.buildNativeParameterSpec(typeParameterResolver) }

private fun KSValueParameter.buildNativeParameterSpec(typeParameterResolver: TypeParameterResolver) =
    ParameterSpec.builder(name!!.asString(), type.toTypeName(typeParameterResolver), modifiers)
        .build()

private fun FunSpec.Builder.implementsFlow(
    elementType: TypeName,
    name: String,
    arguments: String
): FunSpec.Builder {
    val flowType = KONTINUITY_FLOW.parameterizedBy(elementType)
    val toFlow = TO_KONTINUITY_FLOW_FUNCTION

    return this
        .returns(flowType)
        .addStatement("return wrapped.$name($arguments).%M()", toFlow)
}

private fun FunSpec.Builder.implementsStateFlow(
    elementType: TypeName,
    name: String,
    arguments: String
): FunSpec.Builder {
    val stateFlowType = KONTINUITY_STATE_FLOW.parameterizedBy(elementType)
    val toStateFlow = TO_KONTINUITY_STATE_FLOW_FUNCTION

    return this
        .returns(stateFlowType)
        .addStatement("return wrapped.$name($arguments).%M()", toStateFlow)
}

private fun FunSpec.Builder.implementsSuspend(
    type: TypeName,
    name: String,
    arguments: String
): FunSpec.Builder {
    val suspendType = KONTINUITY_SUSPEND.parameterizedBy(type)
    val suspend = KONTINUITY_SUSPEND_FUNCTION

    return this
        .returns(suspendType)
        .addStatement("return %M·{ wrapped.$name($arguments) }", suspend)
}

private fun FunSpec.Builder.implementsSuspendFlow(
    elementType: TypeName,
    name: String,
    arguments: String
): FunSpec.Builder {
    val flowType = KONTINUITY_FLOW.parameterizedBy(elementType)
    val suspendType = KONTINUITY_SUSPEND.parameterizedBy(flowType)

    val suspend = KONTINUITY_SUSPEND_FUNCTION
    val toFlow = TO_KONTINUITY_FLOW_FUNCTION

    return this
        .returns(suspendType)
        .addStatement("return %M·{ wrapped.$name($arguments).%M() }", suspend, toFlow)
}

private fun FunSpec.Builder.implementsSuspendStateFlow(
    elementType: TypeName,
    name: String,
    arguments: String
): FunSpec.Builder {
    val stateFlowType = KONTINUITY_STATE_FLOW.parameterizedBy(elementType)
    val suspendType = KONTINUITY_SUSPEND.parameterizedBy(stateFlowType)

    val suspend = KONTINUITY_SUSPEND_FUNCTION
    val toStateFlow = TO_KONTINUITY_STATE_FLOW_FUNCTION

    return this
        .returns(suspendType)
        .addStatement("return %M·{ wrapped.$name($arguments).%M() }", suspend, toStateFlow)
}

private fun KSClassDeclaration.buildNativePropertySpecs(typeParameterResolver: TypeParameterResolver): List<PropertySpec> {
    return getAllProperties()
        .filter { it.isPublic() }
        .map { it.buildNativePropertySpec(typeParameterResolver) }
        .toList()
}

private fun KSPropertyDeclaration.buildNativePropertySpec(typeParameterResolver: TypeParameterResolver): PropertySpec {
    val returnType = type.getReturnType(typeParameterResolver)
    val name = getNativeName(returnType)
//    val name = simpleName.asString()
//    val modifiers = listOf(KModifier.OVERRIDE)
    val modifiers = emptyList<KModifier>()

    return when (returnType) {
        is ReturnType.Value ->
            buildNonFlowNativePropertySpec(name, returnType.type, modifiers)

        is ReturnType.Flow ->
            buildFlowNativePropertySpec(name, returnType.elementType, modifiers)

        is ReturnType.StateFlow ->
            buildStateFlowNativePropertySpec(name, returnType.elementType, modifiers)

        else -> throw IllegalStateException("Unknown return type ${returnType::class}")
    }
}

private fun KSPropertyDeclaration.buildNonFlowNativePropertySpec(
    name: String,
    propertyDecType: TypeName,
    modifiers: List<KModifier>
) = PropertySpec.builder(name, propertyDecType, modifiers)
    .mutable(isMutable)
    .getter(
        FunSpec.getterBuilder()
            .addStatement("return wrapped.${simpleName.asString()}")
            .build()
    )
    .setter(
        setter?.let {
            FunSpec.setterBuilder()
                .addParameter("newValue", propertyDecType)
                .addStatement("wrapped.${simpleName.asString()} = newValue")
                .build()
        }
    )
    .build()

private fun KSPropertyDeclaration.buildFlowNativePropertySpec(
    name: String,
    elementType: TypeName,
    modifiers: List<KModifier>
) = PropertySpec.builder(name, KONTINUITY_FLOW.parameterizedBy(elementType), modifiers)
    .getter(
        FunSpec.getterBuilder()
            .addStatement(
                "return wrapped.${simpleName.asString()}.%M()",
                TO_KONTINUITY_FLOW_FUNCTION
            )
            .build()
    )
    .build()

private fun KSPropertyDeclaration.buildStateFlowNativePropertySpec(
    name: String,
    elementType: TypeName,
    modifiers: List<KModifier>
) = PropertySpec.builder(name, KONTINUITY_STATE_FLOW.parameterizedBy(elementType), modifiers)
    .getter(
        FunSpec.getterBuilder()
            .addStatement(
                "return wrapped.${simpleName.asString()}.%M()",
                TO_KONTINUITY_STATE_FLOW_FUNCTION
            )
            .build()
    )
    .build()
