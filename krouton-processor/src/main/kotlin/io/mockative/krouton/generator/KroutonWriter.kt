package io.mockative.krouton.generator

import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.Modifier
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import io.mockative.krouton.isFlow
import java.io.OutputStreamWriter

interface Logger {
    fun info(message: String)
    fun debug(message: String)
}

object ClassNames {
    val flow = ClassName("kotlinx.coroutines.flow", "Flow")
}

@OptIn(KotlinPoetKspPreview::class)
class KroutonWriter(
    private val writer: OutputStreamWriter,
    private val logger: Logger? = null
) {
    var numberOfWrittenProperties = 0
        private set

    var numberOfWrittenFunctions = 0
        private set

    fun writeKroutons(classDec: KSClassDeclaration) {
        val packageName = classDec.packageName.asString()
        val containingFileName = classDec.containingFile!!.fileName
        val fileName = "${containingFileName}Kroutons"

        val fileSpec = FileSpec.builder(packageName, fileName)

        // Properties
        classDec.getDeclaredProperties()
            .forEach { property -> writeKroutonProperty(fileSpec, classDec, property) }

        // Functions
        classDec.getDeclaredFunctions()
            .forEach { function -> writeKroutonFunction(classDec, function) }
    }

    private fun writeKroutonProperty(fileSpec: FileSpec.Builder, classDec: KSClassDeclaration, property: KSPropertyDeclaration) {
        // Nothing yet
        when {
            property.type.isFlow -> writeKroutonFlowProperty(fileSpec, classDec, property)
        }
    }

    private fun writeKroutonFlowProperty(
        fileSpec: FileSpec.Builder,
        classDec: KSClassDeclaration,
        property: KSPropertyDeclaration
    ) {
        // Write Flow Property
        val className = classDec.toClassName()
        val propertyName = property.simpleName.asString()
        logger?.debug("Generating Krouton property for `${className}.${propertyName}`")

        val propertyTypeName = property.type.toTypeName()
        if (propertyTypeName !is ParameterizedTypeName) {
            return
        }

        if (propertyTypeName.rawType != ClassNames.flow) {
            return
        }

        val elementTypeName = propertyTypeName.typeArguments[0]

        val funSpec = FunSpec.builder(property.simpleName.asString())
            .addParameter("receiver", className)
            .addParameter(
                "onElement", LambdaTypeName.get(
                    parameters = arrayOf(elementTypeName),
                    returnType = UNIT
                )
            )
            .addParameter(
                "onSuccess", LambdaTypeName.get(
                    parameters = arrayOf(UNIT),
                    returnType = UNIT
                )
            )
            .addParameter(
                "onFailure", LambdaTypeName.get(
                    parameters = arrayOf(THROWABLE),
                    returnType = UNIT
                )
            )
            .returns(UNIT)
            .build()

        fileSpec.addFunction(funSpec)

        logger?.debug("Writing: $funSpec")

        numberOfWrittenProperties += 1
    }

    private fun writeKroutonFunction(classDec: KSClassDeclaration, function: KSFunctionDeclaration) {
        when {
            function.modifiers.contains(Modifier.SUSPEND) -> writeKroutonSuspendFunction(classDec, function)
            function.returnType!!.isFlow -> writeKroutonFlowFunction(classDec, function)
        }
    }

    private fun writeKroutonSuspendFunction(classDec: KSClassDeclaration, function: KSFunctionDeclaration) {
        // Write Suspend Function
        val className = classDec.simpleName.asString()

        val functionName = "${className}_${function}"
        logger?.debug("Generating Krouton function $functionName")

        val parameterList = if (function.parameters.isEmpty()) "" else ", " + function.parameters.joinToString(", ") { parameter ->
            "${parameter.name!!.asString()}: ${parameter.type.resolve()}"
        }

        val returnType = function.returnType?.resolve().toString()

        val declaration = "fun $functionName(receiver: ${className}${parameterList}, onSuccess: (${returnType}) -> Unit, onFailure: (Throwable) -> Unit) ="
        writer.appendLine(declaration)

        val argumentList = if (function.parameters.isEmpty()) "" else function.parameters.joinToString(", ") { parameter -> parameter.name!!.asString() }
        val definition = "invokeSuspend({ receiver.${functionName}($argumentList) }, onSuccess, onFailure)"
        writer.appendLine("   $definition")

        writer.appendLine()

        logger?.debug("$declaration $definition")

        numberOfWrittenFunctions += 1
    }

    private fun writeKroutonFlowFunction(classDec: KSClassDeclaration, function: KSFunctionDeclaration) {
        // Write Flow Function
        val className = classDec.simpleName.asString()

        val functionName = "${className}_${function}"
        logger?.debug("Generating Krouton function $functionName")

        val parameterList = if (function.parameters.isEmpty()) "" else ", " + function.parameters.joinToString(", ") { parameter ->
            "${parameter.name!!.asString()}: ${parameter.type.resolve()}"
        }

        val elementType = function.returnType?.resolve().toString()
            .removePrefix("Flow<")
            .removeSuffix(">")

        val declaration = "fun $functionName(receiver: ${className}${parameterList}, onElement: (${elementType}) -> Unit, onSuccess: (Unit) -> Unit, onFailure: (Throwable) -> Unit) ="
        writer.appendLine(declaration)

        val argumentList = if (function.parameters.isEmpty()) "" else function.parameters.joinToString(", ") { parameter -> parameter.name!!.asString() }
        val definition = "collectFlow(receiver.${functionName}($argumentList), onElement, onSuccess, onFailure)"
        writer.appendLine("   $definition")

        writer.appendLine()

        logger?.debug("$declaration $definition")

        numberOfWrittenFunctions += 1
    }
}


