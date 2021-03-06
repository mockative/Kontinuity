package io.mockative.kontinuity.kotlinpoet

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import io.mockative.kontinuity.*

internal fun ProcessableProperty.buildPropertySpec(): PropertySpec {
    return when (type) {
        is ReturnType.Value ->
            buildNonFlowPropertySpec(type.type)

        is ReturnType.Flow ->
            buildFlowPropertySpec(type.elementType)

        is ReturnType.StateFlow ->
            buildStateFlowPropertySpec(type.elementType)

        else -> throw IllegalStateException("Unknown return type ${type::class}")
    }
}

internal fun ProcessableProperty.buildNonFlowPropertySpec(
    type: TypeName
): PropertySpec {
    val getter = FunSpec.getterBuilder()
        .addStatement("return wrapped.$sourceMemberName")
        .build()

    val setter = if (isMutable) {
        FunSpec.setterBuilder()
            .addParameter("newValue", type)
            .addStatement("wrapped.$sourceMemberName = newValue")
            .build()
    } else {
        null
    }

    return PropertySpec.builder(wrapperMemberName, type)
        .addModifiers(KModifier.OPEN)
        .mutable(isMutable)
        .getter(getter)
        .setter(setter)
        .build()
}

internal fun ProcessableProperty.buildFlowPropertySpec(
    elementType: TypeName
): PropertySpec {
    val builder = FunSpec.getterBuilder()

    if (scopeDeclaration != null) {
        val scopeMember = scopeDeclaration.toMemberName()
        builder.addStatement("return wrapped.$sourceMemberName.%M(%M)", TO_KONTINUITY_FLOW_FUNCTION, scopeMember)
    } else {
        builder.addStatement("return wrapped.$sourceMemberName.%M()", TO_KONTINUITY_FLOW_FUNCTION)
    }

    val getter = builder.build()

    val type = KONTINUITY_FLOW.parameterizedBy(elementType)

    return PropertySpec.builder(wrapperMemberName, type)
        .addModifiers(KModifier.OPEN)
        .getter(getter)
        .build()
}

internal fun ProcessableProperty.buildStateFlowPropertySpec(
    elementType: TypeName
): PropertySpec {
    val builder = FunSpec.getterBuilder()

    if (scopeDeclaration != null) {
        val scopeMember = scopeDeclaration.toMemberName()
        builder.addStatement("return wrapped.$sourceMemberName.%M(%M)", TO_KONTINUITY_STATE_FLOW_FUNCTION, scopeMember)
    } else {
        builder.addStatement("return wrapped.$sourceMemberName.%M()", TO_KONTINUITY_STATE_FLOW_FUNCTION)
    }

    val getter = builder.build()

    val type = KONTINUITY_STATE_FLOW.parameterizedBy(elementType)

    return PropertySpec.builder(wrapperMemberName, type)
        .addModifiers(KModifier.OPEN)
        .getter(getter)
        .build()
}

