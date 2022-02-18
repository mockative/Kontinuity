package io.mockative.krouton.generator

import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.*

class KroutonClassWriter(
    private val codeGenerator: CodeGenerator,
    private val logger: Logger?,
    private val classDec: KSClassDeclaration
) {
    private val classTypeParameterResolver: TypeParameterResolver =
        classDec.typeParameters.toTypeParameterResolver()

    private val className: ClassName =
        classDec.toClassName()

    var numberOfWrittenProperties = 0
        private set

    var numberOfWrittenFunctions = 0
        private set

    private lateinit var fileSpec: FileSpec.Builder

    fun writeKroutons() {
        val packageName = className.packageName
        val fileName = className.simpleNames.joinToString(".")

        fileSpec = FileSpec.builder(packageName, fileName)

        // Properties
        classDec.getDeclaredProperties()
            .forEach { property -> addKroutonProperty(property) }

        // Functions
        classDec.getDeclaredFunctions()
            .forEach { function -> addKroutonFunction(function) }

        fileSpec
            .build()
            .writeTo(codeGenerator, Dependencies(false, classDec.containingFile!!))
    }

    private fun addKroutonProperty(property: KSPropertyDeclaration) {
        addCollectingFlowPropertyFunction(property)
    }

    /**
     * Adds a function that collects the flow of the specified property.
     *
     * If the specified property is not a `Flow` property, the property will be skipped.
     *
     * @param property The property declaration to add a flow collector for
     */
    private fun addCollectingFlowPropertyFunction(property: KSPropertyDeclaration) {
        // Write Flow Property
        val propertyName = property.simpleName.asString()
        logger?.debug("Generating Krouton property for ${className}.${propertyName}")

        // The TypeParameterResolver is used to resolve type names
        val typeParameterResolver = property.typeParameters
            .toTypeParameterResolver(classTypeParameterResolver)

        val elementTypeName = property.type
            .getFlowElementTypeNameOrNull(property, typeParameterResolver) ?: return

        fileSpec
            .addImport("io.mockative.krouton", "collectFlow")
            .addFunction(
                FunSpec.builder(propertyName)
                    .addTypeVariables(property.getTypeVariables(typeParameterResolver))
                    .addParameter("receiver", className)
                    .addParameter("onElement", getOnElementType(elementTypeName))
                    .addParameter("onSuccess", getOnSuccessType())
                    .addParameter("onFailure", getOnFailureType())
                    .returns(CANCELLATION)
                    .addCode(getFlowPropertyBody(propertyName))
                    .build()
            )


        numberOfWrittenProperties += 1
    }

    private fun getFlowPropertyBody(propertyName: String): CodeBlock {
        return CodeBlock.builder()
            .add("return collectFlow(receiver.${propertyName}, onElement, onSuccess, onFailure)")
            .build()
    }

    private fun addKroutonFunction(function: KSFunctionDeclaration) {
        when {
            function.modifiers.contains(Modifier.SUSPEND) -> addSuspendWrapperFunction(function)
            else -> addCollectingFlowFunctionFunction(function)
        }
    }

    /**
     * Adds a suspend wrapper function to the [fileSpec].
     *
     * @param function The function declaration to add a suspend wrapper function for
     */
    private fun addSuspendWrapperFunction(function: KSFunctionDeclaration) {
        val functionName = function.simpleName.asString()
        logger?.debug("Generating Krouton function for `$className.$functionName`")

        // The TypeParameterResolver is used to resolve type names
        val typeParameterResolver = function.typeParameters
            .toTypeParameterResolver(classTypeParameterResolver)

        // The function return type is used as the type argument for the OnSuccess callback
        val returnType = function.getReturnType()
        val returnTypeName = returnType.toTypeName(typeParameterResolver)
        val actualReturnTypeName = if (returnTypeName == UNIT) null else returnTypeName

        fileSpec
            .addImport("io.mockative.krouton", "invokeSuspend")
            .addFunction(
                FunSpec.builder(functionName)
                    .addTypeVariables(function.getTypeVariables(typeParameterResolver))
                    .addParameter("receiver", className)
                    .addParameters(function.getParameterSpecs(typeParameterResolver))
                    .addParameter("onSuccess", getOnSuccessType(actualReturnTypeName))
                    .addParameter("onFailure", getOnFailureType())
                    .returns(CANCELLATION)
                    .addCode(getSuspendWrapperFunctionBody(function))
                    .build()
            )

        numberOfWrittenFunctions += 1
    }

    private fun getSuspendWrapperFunctionBody(function: KSFunctionDeclaration): CodeBlock {
        val functionName = function.simpleName.asString()
        val argumentList = function.getArgumentList()

        return CodeBlock.builder()
            .add("return invokeSuspend({ receiver.$functionName(${argumentList}) }, onSuccess, onFailure)")
            .build()
    }

    private fun addCollectingFlowFunctionFunction(function: KSFunctionDeclaration) {
        val functionName = function.simpleName.asString()
        logger?.debug("Generating Krouton function for `$className.$functionName`")

        // The TypeParameterResolver is used to resolve type names
        val typeParameterResolver = function.typeParameters
            .toTypeParameterResolver(classTypeParameterResolver)

        val elementTypeName = function.getReturnType()
            .getFlowElementTypeNameOrNull(function, typeParameterResolver) ?: return

        fileSpec
            .addImport("io.mockative.krouton", "collectFlow")
            .addFunction(
                FunSpec.builder(functionName)
                    .addTypeVariables(function.getTypeVariables(typeParameterResolver))
                    .addParameter("receiver", className)
                    .addParameters(function.getParameterSpecs(typeParameterResolver))
                    .addParameter("onElement", getOnElementType(elementTypeName))
                    .addParameter("onSuccess", getOnSuccessType())
                    .addParameter("onFailure", getOnFailureType())
                    .returns(CANCELLATION)
                    .addCode(getFlowFunctionBody(function))
                    .build()
            )

        numberOfWrittenFunctions += 1
    }

    private fun KSFunctionDeclaration.getReturnType(): KSTypeReference =
        returnType ?: throw Errors.functionReturnTypeCouldNotBeResolved(classDec, this)

    private fun getFlowFunctionBody(function: KSFunctionDeclaration): CodeBlock {
        val functionName = function.simpleName.asString()
        val argumentList = function.getArgumentList()

        return CodeBlock.builder()
            .add("return collectFlow(receiver.${functionName}(${argumentList}), onElement, onSuccess, onFailure)")
            .build()
    }

    private fun getOnElementType(type: TypeName) = LambdaTypeName.get(
        parameters = arrayOf(type),
        returnType = UNIT
    )

    private fun getOnSuccessType(type: TypeName? = null) = LambdaTypeName.get(
        parameters = if (type == null) emptyArray() else arrayOf(type),
        returnType = UNIT
    )

    private fun getOnFailureType() = LambdaTypeName.get(
        parameters = arrayOf(THROWABLE),
        returnType = UNIT
    )

    private fun KSDeclaration.getTypeVariables(
        typeParameterResolver: TypeParameterResolver
    ) = typeParameters
        .map { typeParameter -> typeParameter.toTypeVariableName(typeParameterResolver) }

    private fun KSFunctionDeclaration.getParameterSpecs(
        typeParameterResolver: TypeParameterResolver
    ) = parameters.map { parameter ->
        ParameterSpec
            .builder(
                name = parameter.name!!.asString(),
                type = parameter.type.toTypeName(typeParameterResolver),
                modifiers = parameter.modifiers
            )
            .build()
    }

    private fun KSFunctionDeclaration.getArgumentList() = when {
        parameters.isEmpty() -> ""
        else -> parameters.withIndex().joinToString(", ") { (index, parameter) ->
            getParameterName(index, parameter)
        }
    }

    private fun KSFunctionDeclaration.getParameterName(
        index: Int,
        parameter: KSValueParameter
    ): String {
        val parameterName = parameter.name
            ?: throw Errors.functionParameterMissingName(classDec, this, index)

        return parameterName.asString()
    }

    private fun KSTypeReference.getFlowElementTypeNameOrNull(
        declaration: KSDeclaration,
        typeParameterResolver: TypeParameterResolver
    ): TypeName? {
        val typeName = toTypeName(typeParameterResolver)
        if (typeName !is ParameterizedTypeName) {
            logger?.debug("Skipping declaration `${declaration.simpleName.asString()}` because it is `TypeName` is not an instance of `ParameterizedTypeName`. Was: `${typeName.javaClass.canonicalName}`.")
            return null
        }

        if (!typeName.rawType.isFlow) {
            logger?.debug("Skipping declaration `${declaration.simpleName.asString()}` because its raw type is not a known `Flow` type. Was: `${typeName.rawType}`.")
            return null
        }

        return typeName.typeArguments[0]
    }

}