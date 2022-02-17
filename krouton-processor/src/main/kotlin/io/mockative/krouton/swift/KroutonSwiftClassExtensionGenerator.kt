package io.mockative.krouton.swift

import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.*
import io.mockative.krouton.generator.Errors
import io.mockative.krouton.generator.Logger
import io.mockative.krouton.generator.isFlow
import io.mockative.krouton.generator.modifiers
import io.outfoxx.swiftpoet.*
import com.google.devtools.ksp.symbol.Modifier as KSPModifier

class KroutonSwiftClassExtensionGenerator(
    private val codeGenerator: CodeGenerator,
    private val logger: Logger?,
    private val classDec: KSClassDeclaration,
    private val moduleName: String,
) {
    private val classTypeParameterResolver: TypeParameterResolver =
        classDec.typeParameters.toTypeParameterResolver()

    private val className: ClassName =
        classDec.toClassName()

    private lateinit var fileSpec: FileSpec.Builder
    private lateinit var extensionSpec: ExtensionSpec.Builder

    fun addExtensionFile(outputPackage: String) {
        val fileName = className.simpleNames.joinToString(".")

        fileSpec = FileSpec.builder(fileName)
            .addImport("Foundation")
            .addImport("Combine")
            .addImport(moduleName)

        val extendedTypeSimpleName = className.simpleNames.joinToString(".")
        val extendedTypeName = DeclaredTypeName(moduleName, extendedTypeSimpleName)

        val extendedType = TypeSpec.classBuilder(extendedTypeName)
            .build()

        extensionSpec = ExtensionSpec.builder(extendedType)

        // Properties
        classDec.getDeclaredProperties()
            .forEach { property -> addFlowPropertyExtension(property) }

        // Functions
        classDec.getDeclaredFunctions()
            .forEach { function -> addFunctionExtension(function) }

        val packageName = outputPackage.ifEmpty { className.packageName }
        val dependencies = Dependencies(false, classDec.containingFile!!)

        fileSpec.addExtension(extensionSpec.build())
            .build()
            .writeTo(codeGenerator, packageName, dependencies)
    }

    private fun addFlowPropertyExtension(property: KSPropertyDeclaration) {
        val propertyName = property.simpleName.asString()
        logger?.debug("Adding property extension for ${className}.${propertyName}")

        // The TypeParameterResolver is used to resolve type names
        val typeParameterResolver = property.typeParameters
            .toTypeParameterResolver(classTypeParameterResolver)

        val elementKotlinTypeName = property.type
            .getFlowElementKotlinTypeNameOrNull(property, typeParameterResolver) ?: return

        val elementSwiftTypeName = elementKotlinTypeName.toSwiftTypeName(moduleName)

        val swiftPropertyTypeName = DeclaredTypeName.typeName("Combine.AnyPublisher")
            .parameterizedBy(
                TypeVariableName.typeVariable(elementSwiftTypeName.toString()),
                TypeVariableName.typeVariable("Swift.Error")
            )

        val kroutonClassSwiftTypeName = className.simpleNames.joinToString("_") + "Kt"

        extensionSpec.addProperty(
            PropertySpec.builder(propertyName, swiftPropertyTypeName)
                .getter(
                    FunctionSpec.getterBuilder()
                        .addCode(
                            CodeBlock.builder()
                                .addStatement("KroutonPublisher { ${kroutonClassSwiftTypeName}_${propertyName}(receiver: self, onElement: $0, onSuccess: $1, onFailure: $2) }")
                                .build()
                        )
                        .build()
                )
                .build()
        )
    }

    private fun addFunctionExtension(function: KSFunctionDeclaration) {
        when {
            function.modifiers.contains(KSPModifier.SUSPEND) -> addFutureFunctionExtension(function)
            else -> {}
        }
    }

    private fun addFutureFunctionExtension(function: KSFunctionDeclaration) {
        val functionName = function.simpleName.asString()
        logger?.debug("Adding property extension for ${className}.${functionName}")

        // The TypeParameterResolver is used to resolve type names
        val kotlinTypeParameterResolver = function.typeParameters
            .toTypeParameterResolver(classTypeParameterResolver)

        // The function return type is used as the type argument for the OnSuccess callback
        val kotlinReturnType = function.getReturnType()
        val kotlinReturnTypeName = kotlinReturnType.toTypeName(kotlinTypeParameterResolver)

        val swiftReturnTypeName = DeclaredTypeName.typeName("Combine.Future")
            .parameterizedBy(
                TypeVariableName.typeVariable(kotlinReturnTypeName.toSwiftTypeName(moduleName).toString()),
                TypeVariableName.typeVariable("Swift.Error")
            )

        val kroutonClassSwiftTypeName = className.simpleNames.joinToString("_") + "Kt"

        val swiftArgumentList = if (function.parameters.isEmpty()) "" else {
            function.parameters.joinToString { parameter -> ", $parameter: $parameter" }
        }

        extensionSpec.addFunction(
            FunctionSpec.builder(functionName)
                .addTypeVariables(
                    function.typeParameters
                        .map { it.toTypeVariableName(kotlinTypeParameterResolver) }
                        .map { it.toSwiftTypeVariableName() }
                )
                .addParameters(
                    function.parameters.map {
                        ParameterSpec
                            .builder(
                                parameterName = it.name!!.asString(),
                                type = it.type
                                    .toTypeName(kotlinTypeParameterResolver)
                                    .toSwiftTypeName(moduleName),
                                modifiers = it.modifiers
                                    .toSwiftModifiers()
                                    .toTypedArray()
                            )
                            .build()
                    }
                )
                .addCode(
                    CodeBlock.builder()
                        .addStatement("Future { resolve in ${kroutonClassSwiftTypeName}_${functionName}(receiver: self${swiftArgumentList}, onSuccess: { resolve(.success($0)) }, onFailure: { resolve(.failure($2)) }) }")
                        .build()
                )
                .returns(swiftReturnTypeName)
                .build()
        )
    }

    private fun KSFunctionDeclaration.getReturnType(): KSTypeReference =
        returnType ?: throw Errors.functionReturnTypeCouldNotBeResolved(classDec, this)

    private fun KSTypeReference.getFlowElementKotlinTypeNameOrNull(
        declaration: KSDeclaration,
        typeParameterResolver: TypeParameterResolver
    ): TypeName? {
        val typeName = toTypeName(typeParameterResolver)
        if (typeName !is ParameterizedTypeName) {
            logger?.debug("Skipping declaration `${declaration.simpleName.asString()}` because its `TypeName` is not an instance of `ParameterizedTypeName`. Was: `${typeName.javaClass.canonicalName}`.")
            return null
        }

        if (!typeName.rawType.isFlow) {
            logger?.debug("Skipping declaration `${declaration.simpleName.asString()}` because its raw type is not a known `Flow` type. Was: `${typeName.rawType}`.")
            return null
        }

        return typeName.typeArguments[0]
    }
}