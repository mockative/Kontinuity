package io.mockative.krouton.generator

import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.*

fun KSClassDeclaration.buildNativeWrapperTypeSpec(
    className: ClassName,
    superinterface: ClassName
): TypeSpec {
    val typeParameterResolver = typeParameters.toTypeParameterResolver()

    val wrappedPropertySpec = buildWrappedPropertySpec()

    return TypeSpec.classBuilder(className)
        .addModifiers(KModifier.OPEN)
        .addSuperinterface(superinterface)
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

private fun KSFunctionDeclaration.buildNativeFunSpec(typeParameterResolver: TypeParameterResolver): FunSpec {
    val name = simpleName.asString()

    val functionType = getFunctionType(typeParameterResolver)
    val nativeName = getNativeName(functionType)

    val builder = FunSpec.builder(nativeName)
        .addModifiers(KModifier.OVERRIDE)
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

    val arguments = parameters.joinToString(", ") { parameter -> parameter.name!!.asString() }

    return when (functionType) {
        is FunctionType.Blocking -> when (val returnType = functionType.returnType) {
            is ReturnType.Value -> builder
                .returns(returnType.type)
                .addStatement("return wrapped.$name($arguments)")
                .build()
            is ReturnType.Flow -> builder
                .returns(KONTINUITY_FLOW.parameterizedBy(returnType.elementType))
                .addStatement("return wrapped.$name($arguments).%M()", TO_KONTINUITY_FLOW_FUNCTION)
                .build()
            is ReturnType.StateFlow -> builder
                .returns(KONTINUITY_STATE_FLOW.parameterizedBy(returnType.elementType))
                .addStatement("return wrapped.$name($arguments).%M()", TO_KONTINUITY_STATE_FLOW_FUNCTION)
                .build()
            else -> throw IllegalStateException("Unknown return type ${returnType::class}")
        }
        is FunctionType.Suspending -> when (val returnType = functionType.returnType) {
            is ReturnType.Value -> builder
                .returns(KONTINUITY_SUSPEND.parameterizedBy(returnType.type))
                .addStatement("return %M·{ wrapped.$name($arguments) }", KONTINUITY_SUSPEND_FUNCTION)
                .build()
            is ReturnType.Flow -> builder
                .returns(KONTINUITY_SUSPEND.parameterizedBy(KONTINUITY_FLOW.parameterizedBy(returnType.elementType)))
                .addStatement("return %M·{ wrapped.$name($arguments).%M() }", KONTINUITY_SUSPEND_FUNCTION, TO_KONTINUITY_FLOW_FUNCTION)
                .build()
            is ReturnType.StateFlow -> builder
                .returns(KONTINUITY_SUSPEND.parameterizedBy(KONTINUITY_STATE_FLOW.parameterizedBy(returnType.elementType)))
                .addStatement("return %M·{ wrapped.$name($arguments).%M() }", KONTINUITY_SUSPEND_FUNCTION, TO_KONTINUITY_STATE_FLOW_FUNCTION)
                .build()
            else -> throw IllegalStateException("Unknown return type ${returnType::class}")
        }
        else -> throw IllegalStateException("Unknown function type ${functionType::class}")
    }
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
    val modifiers = listOf(KModifier.OVERRIDE)

    return when (returnType) {
        is ReturnType.Value -> buildNonFlowNativePropertySpec(name, returnType.type, modifiers)
        is ReturnType.Flow -> buildFlowNativePropertySpec(name, returnType.elementType, modifiers)
        is ReturnType.StateFlow -> buildStateFlowNativePropertySpec(name, returnType.elementType, modifiers)
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
            .addStatement("return wrapped.${simpleName.asString()}.%M()", TO_KONTINUITY_FLOW_FUNCTION)
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
            .addStatement("return wrapped.${simpleName.asString()}.%M()", TO_KONTINUITY_STATE_FLOW_FUNCTION)
            .build()
    )
    .build()
