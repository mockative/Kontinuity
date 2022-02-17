package io.mockative.krouton.swift

import com.squareup.kotlinpoet.KModifier
import io.outfoxx.swiftpoet.OPTIONAL
import io.outfoxx.swiftpoet.parameterizedBy
import com.squareup.kotlinpoet.BOOLEAN as KOTLIN_BOOLEAN
import com.squareup.kotlinpoet.UNIT as KOTLIN_UNIT
import com.squareup.kotlinpoet.ClassName as KotlinClassName
import com.squareup.kotlinpoet.Dynamic as KotlinDynamic
import com.squareup.kotlinpoet.INT as KOTLIN_INT
import com.squareup.kotlinpoet.LIST as KOTLIN_LIST
import com.squareup.kotlinpoet.LONG as KOTLIN_LONG
import com.squareup.kotlinpoet.LambdaTypeName as KotlinLambdaTypeName
import com.squareup.kotlinpoet.MAP as KOTLIN_MAP
import com.squareup.kotlinpoet.ParameterSpec as KotlinParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName as KotlinParameterizedTypeName
import com.squareup.kotlinpoet.SET as KOTLIN_SET
import com.squareup.kotlinpoet.STRING as KOTLIN_STRING
import com.squareup.kotlinpoet.THROWABLE as KOTLIN_THROWABLE
import com.squareup.kotlinpoet.TypeName as KotlinTypeName
import com.squareup.kotlinpoet.TypeVariableName as KotlinTypeVariableName
import com.squareup.kotlinpoet.WildcardTypeName as KotlinWildcardTypeName
import io.outfoxx.swiftpoet.ARRAY as SWIFT_ARRAY
import io.outfoxx.swiftpoet.BOOL as SWIFT_BOOL
import io.outfoxx.swiftpoet.DICTIONARY as SWIFT_DICTIONARY
import io.outfoxx.swiftpoet.DeclaredTypeName as SwiftDeclaredTypeName
import io.outfoxx.swiftpoet.FunctionTypeName as SwiftFunctionTypeName
import io.outfoxx.swiftpoet.INT as SWIFT_INT
import io.outfoxx.swiftpoet.INT64 as SWIFT_INT64
import io.outfoxx.swiftpoet.Modifier as SwiftModifier
import io.outfoxx.swiftpoet.ParameterSpec as SwiftParameterSpec
import io.outfoxx.swiftpoet.SET as SWIFT_SET
import io.outfoxx.swiftpoet.STRING as SWIFT_STRING
import io.outfoxx.swiftpoet.VOID as SWIFT_VOID
import io.outfoxx.swiftpoet.TypeName as SwiftTypeName
import io.outfoxx.swiftpoet.TypeVariableName as SwiftTypeVariableName

fun KotlinTypeName.toSwiftTypeName(moduleName: String): SwiftTypeName {
    if (isNullable) {
        return OPTIONAL.parameterizedBy(copy(nullable = false).toSwiftTypeName(moduleName))
    }

    return when (this) {
        KOTLIN_UNIT -> SWIFT_VOID
        KOTLIN_BOOLEAN -> SWIFT_BOOL
        KOTLIN_INT -> SWIFT_INT
        KOTLIN_LONG -> SWIFT_INT64
        KOTLIN_THROWABLE -> SWIFT_INT64
        KOTLIN_STRING -> SWIFT_STRING
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
    when (rawType) {
        KOTLIN_LIST -> SWIFT_ARRAY
        KOTLIN_SET -> SWIFT_SET
        KOTLIN_MAP -> SWIFT_DICTIONARY
        else -> rawType.toSwiftDeclaredTypeName(moduleName)
    }
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

