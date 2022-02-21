package io.mockative.krouton.swift

import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.*
import io.mockative.krouton.Async
import io.mockative.krouton.generator.Errors
import io.mockative.krouton.generator.Logger
import io.mockative.krouton.generator.isFlow
import io.mockative.krouton.generator.modifiers
import io.outfoxx.swiftpoet.*
import java.nio.file.Path
import com.google.devtools.ksp.symbol.Modifier as KSPModifier

class KroutonSwiftClassExtensionGenerator(
    private val codeGenerator: CodeGenerator,
    private val logger: Logger?,
    private val classDec: KSClassDeclaration,
    private val moduleName: String,
    private val swiftFlags: SwiftFlags,
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
            .forEach { property -> addFunctionExtension(property) }

        // Functions
        classDec.getDeclaredFunctions()
            .forEach { function -> addFunctionExtension(function) }

        // Write files
        val packageName = outputPackage.ifEmpty { className.packageName }

        if (swiftFlags.outputDir == null) {
            val dependencies = Dependencies(false, classDec.containingFile!!)

            fileSpec.addExtension(extensionSpec.build())
                .build()
                .writeTo(codeGenerator, "krouton", dependencies)
        } else {
            fileSpec.addExtension(extensionSpec.build())
                .build()
                .writeTo(Path.of(packageName.replace(".", "/"), fileSpec.name))
        }
    }

    private fun KSAnnotated.generateAsyncExtensions(): Boolean {
        return when (swiftFlags.generateAsyncExtensions) {
            null -> getAnnotationsByType(Async::class).firstOrNull()?.generate == true
            true -> getAnnotationsByType(Async::class).firstOrNull()?.generate != false
            false -> false
        }
    }

    private fun addFunctionExtension(property: KSPropertyDeclaration) {
        addFlowPropertyExtension(property)

        // Async generation is disabled while investigating the best signature for these,
        // since the `AsyncThrowingStream` returning functions will clash with the
        // non-async functions.
        if (property.generateAsyncExtensions()) {
            addAsyncThrowingStreamPropertyExtension(property)
        }
    }

    private fun addFlowPropertyExtension(property: KSPropertyDeclaration) {
        // The TypeParameterResolver is used to resolve type names
        val kotlinTypeParameterResolver = property.typeParameters
            .toTypeParameterResolver(classTypeParameterResolver)

        val kotlinElementTypeName = property.type
            .getFlowElementKotlinTypeNameOrNull(property, kotlinTypeParameterResolver) ?: return

        val propertyName = property.simpleName.asString()
        logger?.debug("Adding property extension for ${className}.${propertyName}")

        val swiftElementTypeName = kotlinElementTypeName.toSwiftTypeName(moduleName)
        val swiftPropertyTypeName = DeclaredTypeName(moduleName, "KroutonPublisher")
            .parameterizedBy(
                TypeVariableName(swiftElementTypeName.toString()),
                TypeVariableName("Error")
            )

        val kroutonClassSwiftTypeName = className.simpleNames.joinToString("_") + "Kt"

        extensionSpec.addProperty(
            PropertySpec.builder("$propertyName$", swiftPropertyTypeName)
                .getter(
                    FunctionSpec.getterBuilder()
                        .addCode(
                            CodeBlock.builder()
                                .addStatement("KroutonPublisher { ${kroutonClassSwiftTypeName}.${propertyName}(receiver: self, onElement: $0, onSuccess: $1, onFailure: $2) }")
                                .build()
                        )
                        .build()
                )
                .build()
        )
    }

    private fun addAsyncThrowingStreamPropertyExtension(property: KSPropertyDeclaration) {
        // The TypeParameterResolver is used to resolve type names
        val kotlinTypeParameterResolver = property.typeParameters
            .toTypeParameterResolver(classTypeParameterResolver)

        val kotlinElementTypeName = property.type
            .getFlowElementKotlinTypeNameOrNull(property, kotlinTypeParameterResolver) ?: return

        val propertyName = property.simpleName.asString()
        logger?.debug("Adding AsyncThrowingStream property extension for ${className}.${propertyName}")

        val swiftElementTypeName = kotlinElementTypeName.toSwiftTypeName(moduleName)
        val swiftPropertyTypeName = DeclaredTypeName("Combine", "AsyncThrowingPublisher")
            .parameterizedBy(
                DeclaredTypeName(moduleName, "KroutonPublisher")
                    .parameterizedBy(
                        TypeVariableName(swiftElementTypeName.toString()),
                        TypeVariableName("Error")
                    )
            )

        val kroutonClassSwiftTypeName = className.simpleNames.joinToString("_") + "Kt"

        extensionSpec.addProperty(
            PropertySpec.builder("${propertyName}Async", swiftPropertyTypeName)
                .addAttribute(
                    AttributeSpec.builder("available")
                        .addArguments("macOS 12.0", "iOS 15.0", "tvOS 15.0", "watchOS 8.0", "*")
                        .build()
                )
                .getter(
                    FunctionSpec.getterBuilder()
                        .addCode(
                            CodeBlock.builder()
                                .addStatement("KroutonPublisher { ${kroutonClassSwiftTypeName}.${propertyName}(receiver: self, onElement: $0, onSuccess: $1, onFailure: $2) }.values")
                                .build()
                        )
                        .build()
                )
                .build()
        )
    }

    private fun addFunctionExtension(function: KSFunctionDeclaration) {
        when {
            function.modifiers.contains(KSPModifier.SUSPEND) -> {
                addFutureFunctionExtension(function)

                // Async generation is disabled while investigating the best signature for these,
                // since the `AsyncThrowingStream` returning functions will clash with the
                // non-async functions.
                if (function.generateAsyncExtensions()) {
                    addAsyncFunctionExtension(function)
                }
            }
            else -> {
                addFlowFunctionExtension(function)

                // Async generation is disabled while investigating the best signature for these,
                // since the `AsyncThrowingStream` returning functions will clash with the
                // non-async functions.
                if (function.generateAsyncExtensions()) {
                    addAsyncThrowingStreamFunctionExtension(function)
                }
            }
        }
    }

    private fun addFlowFunctionExtension(function: KSFunctionDeclaration) {
        // The TypeParameterResolver is used to resolve type names
        val kotlinTypeParameterResolver = function.typeParameters
            .toTypeParameterResolver(classTypeParameterResolver)

        // The function return type is used as the type argument for the OnSuccess callback
        val kotlinElementTypeName = function.getReturnType()
            .getFlowElementKotlinTypeNameOrNull(function, kotlinTypeParameterResolver) ?: return

        val functionName = function.simpleName.asString()
        logger?.debug("Adding Flow function extension for ${className}.${functionName}")

        val swiftElementTypeName = kotlinElementTypeName.toSwiftTypeName(moduleName)
        val swiftPropertyTypeName = DeclaredTypeName(moduleName, "KroutonPublisher")
            .parameterizedBy(
                TypeVariableName(swiftElementTypeName.toString()),
                TypeVariableName("Error")
            )

        val kroutonClassSwiftTypeName = className.simpleNames.joinToString("_") + "Kt"

        val swiftArgumentList = if (function.parameters.isEmpty()) "" else {
            function.parameters.joinToString { parameter -> ", $parameter: $parameter" }
        }

        extensionSpec.addFunction(
            FunctionSpec.builder("$functionName$")
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
                        .addStatement("KroutonPublisher { ${kroutonClassSwiftTypeName}.${functionName}(receiver: self${swiftArgumentList}, onElement: $0, onSuccess: $1, onFailure: $2) }")
                        .build()
                )
                .applyIfNotNull(function.docString) { addDoc(it.toSwiftDocString()) }
                .returns(swiftPropertyTypeName)
                .build()
        )
    }

    private inline fun <T, V : Any> T.applyIfNotNull(value: V?, block: T.(V) -> Unit): T {
        return apply {
            if (value != null) {
                block(value)
            }
        }
    }

    private fun String.toSwiftDocString(): String {
        return replace(Regex("""@param (\w+) """), "- Parameter $1: ")
    }

    private fun addAsyncThrowingStreamFunctionExtension(function: KSFunctionDeclaration) {
        // The TypeParameterResolver is used to resolve type names
        val kotlinTypeParameterResolver = function.typeParameters
            .toTypeParameterResolver(classTypeParameterResolver)

        // The function return type is used as the type argument for the OnSuccess callback
        val kotlinElementTypeName = function.getReturnType()
            .getFlowElementKotlinTypeNameOrNull(function, kotlinTypeParameterResolver) ?: return

        val functionName = function.simpleName.asString()
        logger?.debug("Adding async Flow function extension for ${className}.${functionName}")

        val swiftElementTypeName = kotlinElementTypeName.toSwiftTypeName(moduleName)
        val swiftPropertyTypeName = DeclaredTypeName("Combine", "AsyncThrowingPublisher")
            .parameterizedBy(
                DeclaredTypeName(moduleName, "KroutonPublisher")
                    .parameterizedBy(
                        TypeVariableName(swiftElementTypeName.toString()),
                        TypeVariableName("Error")
                    )
            )

        val kroutonClassSwiftTypeName = className.simpleNames.joinToString("_") + "Kt"

        val swiftArgumentList = if (function.parameters.isEmpty()) "" else {
            function.parameters.joinToString { parameter -> ", $parameter: $parameter" }
        }

        extensionSpec.addFunction(
            FunctionSpec.builder("${functionName}Async")
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
                        .addStatement("KroutonPublisher { ${kroutonClassSwiftTypeName}.${functionName}(receiver: self${swiftArgumentList}, onElement: $0, onSuccess: $1, onFailure: $2) }.values")
                        .build()
                )
                .returns(swiftPropertyTypeName)
                .build()
        )
    }

    private fun addFutureFunctionExtension(function: KSFunctionDeclaration) {
        val functionName = function.simpleName.asString()
        logger?.debug("Adding Future function extension for ${className}.${functionName}")

        // The TypeParameterResolver is used to resolve type names
        val kotlinTypeParameterResolver = function.typeParameters
            .toTypeParameterResolver(classTypeParameterResolver)

        // The function return type is used as the type argument for the OnSuccess callback
        val kotlinReturnType = function.getReturnType()
        val kotlinReturnTypeName = kotlinReturnType.toTypeName(kotlinTypeParameterResolver)

        val swiftReturnTypeName = DeclaredTypeName(moduleName, "KroutonFuture")
            .parameterizedBy(
                TypeVariableName(kotlinReturnTypeName.toSwiftTypeName(moduleName).toString()),
                TypeVariableName("Error")
            )

        val kroutonClassSwiftTypeName = className.simpleNames.joinToString("_") + "Kt"

        val swiftArgumentList = if (function.parameters.isEmpty()) "" else {
            function.parameters.joinToString { parameter -> ", $parameter: $parameter" }
        }

        extensionSpec.addFunction(
            FunctionSpec.builder("$functionName$")
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
                        .addStatement("KroutonFuture { ${kroutonClassSwiftTypeName}.${functionName}(receiver: self${swiftArgumentList}, onSuccess: $0, onFailure: $1) }")
                        .build()
                )
                .returns(swiftReturnTypeName)
                .build()
        )
    }

    private fun addAsyncFunctionExtension(function: KSFunctionDeclaration) {
        val functionName = function.simpleName.asString()
        logger?.debug("Adding async function extension for ${className}.${functionName}")

        // The TypeParameterResolver is used to resolve type names
        val kotlinTypeParameterResolver = function.typeParameters
            .toTypeParameterResolver(classTypeParameterResolver)

        // The function return type is used as the type argument for the OnSuccess callback
        val kotlinReturnType = function.getReturnType()
        val kotlinReturnTypeName = kotlinReturnType.toTypeName(kotlinTypeParameterResolver)

        val swiftReturnTypeName = kotlinReturnTypeName.toSwiftTypeName(moduleName)

        val swiftArgumentList = if (function.parameters.isEmpty()) "" else {
            ", " + function.parameters.joinToString(", ") { parameter -> "$parameter: $parameter" }
        }

        val kroutonClassSwiftTypeName = className.simpleNames.joinToString("_") + "Kt"

        extensionSpec.addFunction(
            FunctionSpec.builder("${functionName}Async")
                .addAttribute(
                    AttributeSpec.builder("available")
                        .addArguments("macOS 12.0", "iOS 15.0", "tvOS 15.0", "watchOS 8.0", "*")
                        .build()
                )
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
                .async(true)
                .throws(true)
                .addCode(
                    CodeBlock.builder()
                        .addStatement("try await KroutonFuture { ${kroutonClassSwiftTypeName}.${functionName}(receiver: self${swiftArgumentList}, onSuccess: $0, onFailure: $1) }.value")
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