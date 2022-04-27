package io.mockative.kontinuity

import com.squareup.kotlinpoet.*

internal fun ProcessableType.buildWrapperTypeSpec(): TypeSpec {
    val wrappedPropertySpec = buildWrappedPropertySpec()
    val wrappingConstructor = buildWrappingConstructor(wrappedPropertySpec)

    val properties = buildPropertySpecs()
    val functions = buildFunSpecs()

    val dependentFiles = declaration.getAllDependentFiles()

    return TypeSpec.classBuilder(wrapperClassName)
        .addModifiers(KModifier.OPEN)
        .addAnnotation(KONTINUITY_GENERATED_ANNOTATION)
        .addAnnotations(declaration.getAnnotationSpecs())
        .addProperty(wrappedPropertySpec)
        .addEmptyConstructor()
        .addFunction(wrappingConstructor)
        .addProperties(properties)
        .addFunctions(functions)
        .addKdoc(declaration.docString?.trim() ?: "")
        .addOriginatingKSFiles(dependentFiles)
        .build()
}

internal fun ProcessableType.buildWrappedPropertySpec(): PropertySpec {
    return PropertySpec.builder("wrapped", sourceClassName, KModifier.LATEINIT)
        .mutable(true)
        .setter(
            FunSpec.setterBuilder()
                .addModifiers(KModifier.PRIVATE)
                .build()
        )
        .build()
}

internal fun ProcessableType.buildWrappingConstructor(propertySpec: PropertySpec): FunSpec {
    val parameter = ParameterSpec.builder("wrapping", sourceClassName)
        .build()

    return FunSpec.constructorBuilder()
        .addParameter(parameter)
        .addStatement("%N = %N", propertySpec, parameter)
        .build()
}

internal fun ProcessableType.buildPropertySpecs(): List<PropertySpec> {
    return properties
        .map { it.buildPropertySpec() }
        .toList()
}

internal fun ProcessableType.buildFunSpecs(): List<FunSpec> {
    return functions
        .map { it.buildFunSpec() }
        .toList()
}
