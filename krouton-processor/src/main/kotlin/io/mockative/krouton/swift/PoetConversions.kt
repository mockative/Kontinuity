package io.mockative.krouton.swift

import com.squareup.kotlinpoet.*
import io.outfoxx.swiftpoet.parameterizedBy
import com.squareup.kotlinpoet.ClassName as KotlinClassName
import com.squareup.kotlinpoet.Dynamic as KotlinDynamic
import com.squareup.kotlinpoet.LambdaTypeName as KotlinLambdaTypeName
import com.squareup.kotlinpoet.ParameterSpec as KotlinParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName as KotlinParameterizedTypeName
import com.squareup.kotlinpoet.TypeName as KotlinTypeName
import com.squareup.kotlinpoet.TypeVariableName as KotlinTypeVariableName
import com.squareup.kotlinpoet.WildcardTypeName as KotlinWildcardTypeName
import io.outfoxx.swiftpoet.TypeName as SwiftTypeName
import io.outfoxx.swiftpoet.DeclaredTypeName as SwiftDeclaredTypeName
import io.outfoxx.swiftpoet.FunctionTypeName as SwiftFunctionTypeName
import io.outfoxx.swiftpoet.Modifier as SwiftModifier
import io.outfoxx.swiftpoet.ParameterSpec as SwiftParameterSpec
import io.outfoxx.swiftpoet.TypeVariableName as SwiftTypeVariableName

fun KotlinTypeName.toSwiftTypeName(moduleName: String): SwiftTypeName {
    return when (this) {
        is KotlinClassName -> toSwiftDeclaredTypeName(moduleName)
        KotlinDynamic -> throw IllegalArgumentException("A `dynamic` type cannot be represented as a Swift type.")
        is KotlinLambdaTypeName -> toSwiftFunctionTypeName(moduleName)
        is KotlinParameterizedTypeName -> toSwiftParameterizedTypeName(moduleName)
        is KotlinTypeVariableName -> toSwiftTypeVariableName()
        is KotlinWildcardTypeName -> throw IllegalArgumentException("A wildcard type cannot be represented as a Swift type.")
    }
}

fun KotlinTypeVariableName.toSwiftTypeVariableName(): SwiftTypeVariableName {
    // TODO Consider supporting `bounds`, e.g. by implementing a `TypeParameterResolver` for Swift.
    return SwiftTypeVariableName.typeVariable(name)
}

fun Iterable<KotlinTypeName>.toSwiftTypeNames(moduleName: String) =
    map { it.toSwiftTypeName(moduleName) }

fun KotlinClassName.toSwiftDeclaredTypeName(moduleName: String) =
    SwiftDeclaredTypeName(moduleName, simpleNames.first(), *simpleNames.drop(1).toTypedArray())

fun KotlinLambdaTypeName.toSwiftFunctionTypeName(moduleName: String) =
    SwiftFunctionTypeName.get(parameters.toSwiftParameterSpecs(moduleName), returnType.toSwiftTypeName(moduleName))

fun KotlinParameterizedTypeName.toSwiftParameterizedTypeName(moduleName: String) =
    rawType.toSwiftDeclaredTypeName(moduleName)
        .parameterizedBy(*typeArguments.toSwiftTypeNames(moduleName).toTypedArray())

fun Iterable<KotlinParameterSpec>.toSwiftParameterSpecs(moduleName: String) =
    map { it.toSwiftParameterSpec(moduleName) }

fun KotlinParameterSpec.toSwiftParameterSpec(moduleName: String) =
    SwiftParameterSpec.builder(name, type.toSwiftTypeName(moduleName), *modifiers.toSwiftModifiers().toTypedArray()).build()

fun Iterable<KModifier>.toSwiftModifiers(): Set<SwiftModifier> =
    mapNotNull { it.toSwiftModifierOrNull() }.toSet()

fun KModifier.toSwiftModifierOrNull(): SwiftModifier? {
    // TODO Consider whether this makes sense, and whether we need to support more modifiers.
    return when (this) {
        KModifier.PUBLIC -> SwiftModifier.PUBLIC
        KModifier.PROTECTED -> SwiftModifier.PUBLIC
        KModifier.PRIVATE -> SwiftModifier.PRIVATE
        KModifier.INTERNAL -> SwiftModifier.INTERNAL
        KModifier.EXPECT -> null
        KModifier.ACTUAL -> null
        KModifier.FINAL -> SwiftModifier.FINAL
        KModifier.OPEN -> SwiftModifier.OPEN
        KModifier.ABSTRACT -> SwiftModifier.OPEN
        KModifier.SEALED -> SwiftModifier.FINAL
        KModifier.CONST -> SwiftModifier.FINAL
        KModifier.EXTERNAL -> null
        KModifier.OVERRIDE -> SwiftModifier.OVERRIDE
        KModifier.LATEINIT -> null
        KModifier.TAILREC -> null
        KModifier.VARARG -> null
        KModifier.SUSPEND -> null
        KModifier.INNER -> null
        KModifier.ENUM -> null
        KModifier.ANNOTATION -> null
        KModifier.VALUE -> null
        KModifier.FUN -> null
        KModifier.COMPANION -> null
        KModifier.INLINE -> null
        KModifier.NOINLINE -> null
        KModifier.CROSSINLINE -> null
        KModifier.REIFIED -> null
        KModifier.INFIX -> null
        KModifier.OPERATOR -> null
        KModifier.DATA -> null
        KModifier.IN -> null
        KModifier.OUT -> null
    }
}

