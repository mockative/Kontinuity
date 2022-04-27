package io.mockative.kontinuity

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import io.mockative.kontinuity.*

internal fun FunSpec.Builder.implementsFlow(
    elementType: TypeName,
    name: String,
    arguments: String
): FunSpec.Builder {
    val flowType = KONTINUITY_FLOW.parameterizedBy(elementType)
    val toFlow = TO_KONTINUITY_FLOW_FUNCTION

    return this
        .returns(flowType)
        .addStatement("return wrapped.$name($arguments).%M()", toFlow)
}

internal fun FunSpec.Builder.implementsStateFlow(
    elementType: TypeName,
    name: String,
    arguments: String
): FunSpec.Builder {
    val stateFlowType = KONTINUITY_STATE_FLOW.parameterizedBy(elementType)
    val toStateFlow = TO_KONTINUITY_STATE_FLOW_FUNCTION

    return this
        .returns(stateFlowType)
        .addStatement("return wrapped.$name($arguments).%M()", toStateFlow)
}

internal fun FunSpec.Builder.implementsSuspend(
    type: TypeName,
    name: String,
    arguments: String
): FunSpec.Builder {
    val suspendType = KONTINUITY_SUSPEND.parameterizedBy(type)
    val suspend = KONTINUITY_SUSPEND_FUNCTION

    return this
        .returns(suspendType)
        .addStatement("return %M·{ wrapped.$name($arguments) }", suspend)
}

internal fun FunSpec.Builder.implementsSuspendFlow(
    elementType: TypeName,
    name: String,
    arguments: String
): FunSpec.Builder {
    val flowType = KONTINUITY_FLOW.parameterizedBy(elementType)
    val suspendType = KONTINUITY_SUSPEND.parameterizedBy(flowType)

    val suspend = KONTINUITY_SUSPEND_FUNCTION
    val toFlow = TO_KONTINUITY_FLOW_FUNCTION

    return this
        .returns(suspendType)
        .addStatement("return %M·{ wrapped.$name($arguments).%M() }", suspend, toFlow)
}

internal fun FunSpec.Builder.implementsSuspendStateFlow(
    elementType: TypeName,
    name: String,
    arguments: String
): FunSpec.Builder {
    val stateFlowType = KONTINUITY_STATE_FLOW.parameterizedBy(elementType)
    val suspendType = KONTINUITY_SUSPEND.parameterizedBy(stateFlowType)

    val suspend = KONTINUITY_SUSPEND_FUNCTION
    val toStateFlow = TO_KONTINUITY_STATE_FLOW_FUNCTION

    return this
        .returns(suspendType)
        .addStatement("return %M·{ wrapped.$name($arguments).%M() }", suspend, toStateFlow)
}