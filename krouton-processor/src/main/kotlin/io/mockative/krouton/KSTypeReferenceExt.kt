package io.mockative.krouton

import com.google.devtools.ksp.symbol.KSClassifierReference
import com.google.devtools.ksp.symbol.KSTypeReference

val KSTypeReference.isFlow: Boolean
    get() = (element as? KSClassifierReference)?.referencedName() == "Flow"