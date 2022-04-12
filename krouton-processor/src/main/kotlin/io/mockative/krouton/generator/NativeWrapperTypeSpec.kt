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

    return TypeSpec.classBuilder(className)
        .addSuperinterface(superinterface)
        .primaryConstructor(buildConstructorSpec())
        .addProperty(buildWrappedPropertySpec())
        .addProperties(buildNativePropertySpecs(typeParameterResolver))
        .addFunctions(buildNativeFunSpecs(typeParameterResolver))
        .addKdoc(docString?.trim() ?: "")
        .build()
}

private fun KSClassDeclaration.buildWrappedPropertySpec() =
    PropertySpec.builder("wrapped", toClassName(), KModifier.PRIVATE)
        .initializer("wrapped")
        .build()

private fun KSClassDeclaration.buildConstructorSpec() =
    FunSpec.constructorBuilder()
        .addParameter("wrapped", toClassName())
        .build()

private fun KSClassDeclaration.buildNativeFunSpecs(typeParameterResolver: TypeParameterResolver): List<FunSpec> {
    return getAllFunctions()
        .filter { it.isPublic() }
        .map { it.buildNativeFunSpec(typeParameterResolver) }
        .toList()
}

private fun KSFunctionDeclaration.buildNativeFunSpec(typeParameterResolver: TypeParameterResolver): FunSpec {
    val name = simpleName.asString()
    val functionType = getFunctionType(typeParameterResolver)

    val builder = FunSpec.builder(name)
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
        }
        is FunctionType.Suspending -> when (val returnType = functionType.returnType) {
            is ReturnType.Value -> builder
                .returns(KONTINUITY_SUSPEND.parameterizedBy(returnType.type))
                .addStatement("return %M { wrapped.$name($arguments) }", KONTINUITY_SUSPEND_FUNCTION)
                .build()
            is ReturnType.Flow -> builder
                .returns(KONTINUITY_SUSPEND.parameterizedBy(KONTINUITY_FLOW.parameterizedBy(returnType.elementType)))
                .addStatement("return %M { wrapped.$name($arguments).%M() }", KONTINUITY_SUSPEND_FUNCTION, TO_KONTINUITY_FLOW_FUNCTION)
                .build()
        }
    }
}

private fun KSClassDeclaration.buildNativePropertySpecs(typeParameterResolver: TypeParameterResolver): List<PropertySpec> {
    return getAllProperties()
        .filter { it.isPublic() }
        .map { it.buildNativePropertySpec(typeParameterResolver) }
        .toList()
}

private fun KSPropertyDeclaration.buildNativePropertySpec(typeParameterResolver: TypeParameterResolver): PropertySpec {
    val name = simpleName.asString()

    val propertyDecType = type.toTypeName(typeParameterResolver)
    val elementType = propertyDecType.getFlowElementTypeNameOrNull()

    val modifiers = modifiers.mapNotNull { it.toKModifier() } + KModifier.OVERRIDE

    return when (elementType) {
        null -> buildNonFlowNativePropertySpec(name, propertyDecType, modifiers)
        else -> buildFlowNativePropertySpec(name, elementType, modifiers)
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
