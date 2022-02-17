package io.mockative.krouton.swift

import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSTypeReference
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.TypeParameterResolver
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.toTypeParameterResolver
import io.mockative.krouton.generator.Logger
import io.mockative.krouton.generator.isFlow
import io.outfoxx.swiftpoet.*

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
            .forEach { property -> addPropertyExtension(property) }

        // Functions
        val packageName = outputPackage.ifEmpty { className.packageName }
        val dependencies = Dependencies(false, classDec.containingFile!!)

        fileSpec.addExtension(extensionSpec.build())
            .build()
            .writeTo(codeGenerator, packageName, dependencies)
    }

    private fun addPropertyExtension(property: KSPropertyDeclaration) {
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