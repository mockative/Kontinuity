package io.mockative.kontinuity.generator

import com.google.devtools.ksp.*
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.*
import io.mockative.kontinuity.*
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
        .addTypeVariables(buildNativeTypeParameterSpecs(typeParameterResolver))
        .addParameters(buildNativeParameterSpecs(typeParameterResolver))
        .addAnnotations(buildNativeThrowsAnnotationSpecs())

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

internal fun KSDeclaration.toClassName(): ClassName {
    require(!isLocal()) {
        "Local/anonymous classes are not supported!"
    }
    val pkgName = packageName.asString()
    val typesString = checkNotNull(qualifiedName).asString().removePrefix("$pkgName.")

    val simpleNames = typesString
        .split(".")
    return ClassName(pkgName, simpleNames)
}

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
        .addMember("%T", *classNames.toTypedArray())
        .build()

private fun KSType.toClassName() =
    when (val declaration = declaration) {
        is KSClassDeclaration -> declaration.toClassName()
        is KSTypeAlias -> declaration.toClassName()
        else -> null
    }

private fun KSFunctionDeclaration.buildNativeTypeParameterSpecs(
    typeParameterResolver: TypeParameterResolver
) = typeParameters.map { it.toTypeVariableName(typeParameterResolver) }

private fun KSFunctionDeclaration.buildNativeParameterSpecs(
    typeParameterResolver: TypeParameterResolver
) = parameters.map { it.buildNativeParameterSpec(typeParameterResolver) }

private fun KSValueParameter.buildNativeParameterSpec(
    typeParameterResolver: TypeParameterResolver
) = ParameterSpec.builder(name!!.asString(), type.toTypeName(typeParameterResolver), modifiers)
    .build()

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
