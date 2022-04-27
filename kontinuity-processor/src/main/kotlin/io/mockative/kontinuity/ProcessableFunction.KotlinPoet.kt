package io.mockative.kontinuity

import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.toTypeVariableName

internal fun ProcessableFunction.buildArgumentList(): String {
    return declaration.parameters.joinToString(", ") { parameter ->
        parameter.name!!.asString()
    }
}

internal fun ProcessableFunction.buildFunSpec(): FunSpec {
    val memberName = if (override) sourceMemberName else wrapperMemberName

    val builder = FunSpec.builder(memberName)
        .addModifiers(if (override) listOf(KModifier.OVERRIDE) else emptyList())
        .addTypeVariables(buildTypeParameterSpecs())
        .addParameters(buildParameterSpecs())
        .addAnnotations(buildThrowsAnnotationSpecs())

    val arguments = buildArgumentList()

    return when (functionType) {
        is FunctionType.Blocking -> when (val returnType = functionType.returnType) {
            is ReturnType.Value -> builder
                .returns(returnType.type)
                .addStatement("return wrapped.$sourceMemberName($arguments)")
                .build()
            is ReturnType.Flow -> builder
                .implementsFlow(returnType.elementType, sourceMemberName, arguments)
                .build()
            is ReturnType.StateFlow -> builder
                .implementsStateFlow(returnType.elementType, sourceMemberName, arguments)
                .build()
            else -> throw IllegalStateException("Unknown return type ${returnType::class}")
        }
        is FunctionType.Suspending -> when (val returnType = functionType.returnType) {
            is ReturnType.Value -> builder
                .implementsSuspend(returnType.type, sourceMemberName, arguments)
                .build()
            is ReturnType.Flow -> builder
                .implementsSuspendFlow(returnType.elementType, sourceMemberName, arguments)
                .build()
            is ReturnType.StateFlow -> builder
                .implementsSuspendStateFlow(returnType.elementType, sourceMemberName, arguments)
                .build()
            else -> throw IllegalStateException("Unknown return type ${returnType::class}")
        }
        else -> throw IllegalStateException("Unknown function type ${functionType::class}")
    }
}

internal fun ProcessableFunction.buildTypeParameterSpecs(): List<TypeVariableName> {
    return declaration.typeParameters.map { it.toTypeVariableName(typeParameterResolver) }
}

internal fun ProcessableFunction.buildParameterSpecs(): List<ParameterSpec> {
    return declaration.parameters.map { it.buildParameterSpec(typeParameterResolver) }
}

internal fun ProcessableFunction.buildThrowsAnnotationSpecs(): List<AnnotationSpec> {
    return declaration.annotations
        .filter { it.shortName.asString() == KOTLIN_THROWS.simpleName }
        .filter { it.annotationType.resolve().toClassName() == KOTLIN_THROWS }
        .mapNotNull { it.arguments.firstOrNull()?.value as? List<*> }
        .map { value -> value.filterIsInstance<KSType>() }
        .map { types -> types.mapNotNull { it.toClassName() } }
        .map { classNames -> buildThrowsAnnotationSpec(classNames) }
        .toList()
}

internal fun buildThrowsAnnotationSpec(classNames: List<ClassName>): AnnotationSpec {
    return AnnotationSpec.builder(KOTLIN_THROWS)
        .addMember(classNames.joinToString(", ") { "%T::class" }, *classNames.toTypedArray())
        .build()
}