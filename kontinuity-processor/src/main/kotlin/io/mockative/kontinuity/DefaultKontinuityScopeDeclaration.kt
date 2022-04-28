package io.mockative.kontinuity

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSPropertyDeclaration

object DefaultKontinuityScopeDeclaration {
    fun fromResolver(resolver: Resolver): KSPropertyDeclaration? {
        return resolver.getSymbolsWithAnnotation(KONTINUITY_SCOPE_ANNOTATION.canonicalName)
            .mapNotNull { it as? KSPropertyDeclaration }
            .filter { it.parentDeclaration == null }
            .singleOrNull()
    }
}