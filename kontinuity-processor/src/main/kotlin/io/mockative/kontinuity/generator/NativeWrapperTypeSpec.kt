package io.mockative.kontinuity.generator

import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.*
import io.mockative.kontinuity.*
import io.mockative.kontinuity.getFunctionType
import io.mockative.kontinuity.getReturnType

data class SourceClass(val declaration: KSClassDeclaration, val className: ClassName)

data class WrapperClass(val className: ClassName, val source: SourceClass)

fun FileSpec.Builder.addWrapperTypes(classDecs: List<KSClassDeclaration>) =
    classDecs
        .mapNotNull { classDec -> classDec.getWrapperClass() }
        .fold(this) { fileSpec, wrapperClass ->
            fileSpec.addWrapperType(wrapperClass)
        }

private fun FileSpec.Builder.addWrapperType(classDec: WrapperClass) =
    addType(classDec.buildWrapperTypeSpec())

private fun WrapperClass.buildWrapperTypeSpec(): TypeSpec {
    val declaration = source.declaration
    val typeParameterResolver = declaration.typeParameters.toTypeParameterResolver()

    val wrappedPropertySpec = buildWrappedPropertySpec(source)

    return TypeSpec.classBuilder(className)
        .addModifiers(KModifier.OPEN)
        .addProperty(wrappedPropertySpec)
        .addEmptyConstructor()
        .addWrappingConstructor(source, wrappedPropertySpec)
        .addProperties(declaration.buildNativePropertySpecs(typeParameterResolver))
        .addFunctions(declaration.buildNativeFunSpecs(typeParameterResolver))
        .addKdoc(declaration.docString?.trim() ?: "")
        .build()
}

internal fun KSClassDeclaration.getSourceClass() =
    SourceClass(this, toClassName())

private fun buildWrappedPropertySpec(source: SourceClass) =
    PropertySpec.builder("wrapped", source.className, KModifier.LATEINIT)
        .mutable(true)
        .setter(
            FunSpec.setterBuilder()
                .addModifiers(KModifier.PRIVATE)
                .build()
        )
        .build()

private fun TypeSpec.Builder.addEmptyConstructor() =
    addFunction(FunSpec.constructorBuilder().build())

private fun TypeSpec.Builder.addWrappingConstructor(source: SourceClass, propertySpec: PropertySpec): TypeSpec.Builder {
    val parameter = ParameterSpec.builder("wrapping", source.className)
        .build()

    return addFunction(
        FunSpec.constructorBuilder()
            .addParameter(parameter)
            .addStatement("%N = %N", propertySpec, parameter)
            .build()
    )
}

private fun KSClassDeclaration.buildNativeFunSpecs(typeParameterResolver: TypeParameterResolver): List<FunSpec> {
    return getAllFunctions()
        .filter { it.isPublic() }
        .mapNotNull { it.getKontinuityFunctionDeclaration(typeParameterResolver) }
        .map { it.buildNativeFunSpec(typeParameterResolver) }
        .toList()
}

private fun KSFunctionDeclaration.getModifiers(): List<KModifier> =
    when {
        isFromAny() -> listOf(KModifier.OVERRIDE)
        else -> emptyList()
    }

data class KontinuityFunctionDeclaration(
    val function: KSFunctionDeclaration,
    val sourceName: String,
    val wrapperName: String,
    val type: FunctionType,
)

private fun KSFunctionDeclaration.getKontinuityFunctionDeclaration(typeParameterResolver: TypeParameterResolver): KontinuityFunctionDeclaration? {
    val annotation = getAnnotationsByType(Kontinuity::class).firstOrNull()
    val generate = annotation?.generate ?: true
    if (!generate) {
        return null
    }

    val sourceName = simpleName.asString()
    val type = getFunctionType(typeParameterResolver)
    val wrapperName = annotation?.name?.ifEmpty { null } ?: getWrapperName(type)
    return KontinuityFunctionDeclaration(this, sourceName, wrapperName, type)
}

private fun KontinuityFunctionDeclaration.buildNativeFunSpec(typeParameterResolver: TypeParameterResolver): FunSpec {
    val modifiers = function.getModifiers()

    val builder = FunSpec.builder(wrapperName)
        .addModifiers(modifiers)
        .addTypeVariables(function.buildNativeTypeParameterSpecs(typeParameterResolver))
        .addParameters(function.buildNativeParameterSpecs(typeParameterResolver))
        .addAnnotations(function.buildNativeThrowsAnnotationSpecs())

    val arguments = function.parameters.joinToString(", ") { parameter ->
        parameter.name!!.asString()
    }

    return when (type) {
        is FunctionType.Blocking -> when (val returnType = type.returnType) {
            is ReturnType.Value -> builder
                .returns(returnType.type)
                .addStatement("return wrapped.$sourceName($arguments)")
                .build()
            is ReturnType.Flow -> builder
                .implementsFlow(returnType.elementType, sourceName, arguments)
                .build()
            is ReturnType.StateFlow -> builder
                .implementsStateFlow(returnType.elementType, sourceName, arguments)
                .build()
            else -> throw IllegalStateException("Unknown return type ${returnType::class}")
        }
        is FunctionType.Suspending -> when (val returnType = type.returnType) {
            is ReturnType.Value -> builder
                .implementsSuspend(returnType.type, sourceName, arguments)
                .build()
            is ReturnType.Flow -> builder
                .implementsSuspendFlow(returnType.elementType, sourceName, arguments)
                .build()
            is ReturnType.StateFlow -> builder
                .implementsSuspendStateFlow(returnType.elementType, sourceName, arguments)
                .build()
            else -> throw IllegalStateException("Unknown return type ${returnType::class}")
        }
        else -> throw IllegalStateException("Unknown function type ${type::class}")
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
        .mapNotNull { it.getKontinuityPropertyDeclaration(typeParameterResolver) }
        .map { it.buildNativePropertySpec() }
        .toList()
}

data class KontinuityPropertyDeclaration(
    val property: KSPropertyDeclaration,
    val sourceName: String,
    val wrapperName: String,
    val type: ReturnType,
)

private fun KSPropertyDeclaration.getKontinuityPropertyDeclaration(typeParameterResolver: TypeParameterResolver): KontinuityPropertyDeclaration? {
    val annotation = getAnnotationsByType(Kontinuity::class).firstOrNull()
    val generate = annotation?.generate ?: true
    if (!generate) {
        return null
    }

    val sourceName = simpleName.asString()
    val type = type.getReturnType(typeParameterResolver)
    val wrapperName = annotation?.name?.ifEmpty { null } ?: getWrapperName(type)
    return KontinuityPropertyDeclaration(this, sourceName, wrapperName, type)
}

private fun KontinuityPropertyDeclaration.buildNativePropertySpec(): PropertySpec {
    val modifiers = emptyList<KModifier>()

    return when (type) {
        is ReturnType.Value ->
            buildNonFlowNativePropertySpec(type.type, modifiers)

        is ReturnType.Flow ->
            buildFlowNativePropertySpec(type.elementType, modifiers)

        is ReturnType.StateFlow ->
            buildStateFlowNativePropertySpec(type.elementType, modifiers)

        else -> throw IllegalStateException("Unknown return type ${type::class}")
    }
}

private fun KontinuityPropertyDeclaration.buildNonFlowNativePropertySpec(
    propertyDecType: TypeName,
    modifiers: List<KModifier>
) = PropertySpec.builder(wrapperName, propertyDecType, modifiers)
    .mutable(property.isMutable)
    .getter(
        FunSpec.getterBuilder()
            .addStatement("return wrapped.$sourceName")
            .build()
    )
    .setter(
        property.setter?.let {
            FunSpec.setterBuilder()
                .addParameter("newValue", propertyDecType)
                .addStatement("wrapped.$sourceName = newValue")
                .build()
        }
    )
    .build()

private fun KontinuityPropertyDeclaration.buildFlowNativePropertySpec(
    elementType: TypeName,
    modifiers: List<KModifier>
) = PropertySpec.builder(wrapperName, KONTINUITY_FLOW.parameterizedBy(elementType), modifiers)
    .getter(
        FunSpec.getterBuilder()
            .addStatement("return wrapped.$sourceName.%M()", TO_KONTINUITY_FLOW_FUNCTION)
            .build()
    )
    .build()

private fun KontinuityPropertyDeclaration.buildStateFlowNativePropertySpec(
    elementType: TypeName,
    modifiers: List<KModifier>
) = PropertySpec.builder(wrapperName, KONTINUITY_STATE_FLOW.parameterizedBy(elementType), modifiers)
    .getter(
        FunSpec.getterBuilder()
            .addStatement("return wrapped.$sourceName.%M()", TO_KONTINUITY_STATE_FLOW_FUNCTION)
            .build()
    )
    .build()

internal fun KSFunctionDeclaration.buildNativeThrowsAnnotationSpecs() =
    annotations
        .filter { it.shortName.asString() == KOTLIN_THROWS.simpleName }
        .filter { it.annotationType.resolve().toClassName() == KOTLIN_THROWS }
        .mapNotNull { it.arguments.firstOrNull()?.value as? List<*> }
        .map { value -> value.filterIsInstance<KSType>() }
        .map { types -> types.mapNotNull { it.toClassName() } }
        .map { classNames -> buildNativeThrowsAnnotationSpec(classNames) }
        .toList()

private fun buildNativeThrowsAnnotationSpec(classNames: List<ClassName>) =
    AnnotationSpec.builder(KOTLIN_THROWS)
        .addMember(classNames.joinToString(", ") { "%T::class" }, *classNames.toTypedArray())
        .build()

private fun KSType.toClassName() =
    when (val declaration = declaration) {
        is KSClassDeclaration -> declaration.toClassName()
        is KSTypeAlias -> declaration.toClassName()
        else -> null
    }