package io.mockative.kontinuity

import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeParameterResolver
import io.mockative.kontinuity.configuration.ClassConfiguration
import io.mockative.kontinuity.configuration.SourceConfiguration

data class ProcessableType(
    val declaration: KSClassDeclaration,
    val sourceClassName: ClassName,
    val wrapperClassName: ClassName,
    val properties: List<ProcessableProperty>,
    val functions: List<ProcessableFunction>,
    val scopeDeclaration: KSPropertyDeclaration?
) {
    companion object {
        private fun getScopeDeclaration(
            file: KSFile,
            properties: List<KSPropertyDeclaration>,
            defaultScopeDeclaration: KSPropertyDeclaration?,
        ): KSPropertyDeclaration? {
            val classScope = properties
                .singleOrNull { it.isAnnotationPresent(KontinuityScope::class) }

            if (classScope != null) {
                return classScope
            }

            val fileScope = file.declarations
                .mapNotNull { it as? KSPropertyDeclaration }
                .filter { it.isAnnotationPresent(KontinuityScope::class) }
                .singleOrNull()

            if (fileScope != null) {
                return fileScope
            }

            return defaultScopeDeclaration
        }

        private fun fromDeclaration(
            declaration: KSClassDeclaration,
            parentConfiguration: SourceConfiguration,
            defaultScopeDeclaration: KSPropertyDeclaration?,
        ): ProcessableType? {
            val configuration = ClassConfiguration.fromDeclaration(declaration, parentConfiguration)

            if (configuration.generation == KontinuityGeneration.NONE) {
                return null
            }

            return with(declaration) {
                val sourceClassName = toClassName()

                val wrapperClassName = with(sourceClassName) {
                    ClassName(packageName, configuration.wrapper.replace("%T", simpleName))
                }

                val typeParameterResolver = typeParameters
                    .toTypeParameterResolver(sourceTypeHint = sourceClassName.toString())

                val publicProperties = getAllProperties()
                    .filter { it.isPublic() }
                    .toList()

                val scopeDeclaration = getScopeDeclaration(
                    file = containingFile!!,
                    properties = publicProperties,
                    defaultScopeDeclaration = defaultScopeDeclaration,
                )

                val properties = publicProperties
                    .mapNotNull { property ->
                        ProcessableProperty.fromDeclaration(
                            property,
                            configuration,
                            typeParameterResolver,
                            scopeDeclaration,
                        )
                    }
                    .toList()

                val functions = declaration.getAllFunctions()
                    .filter { it.isPublic() }
                    .mapNotNull { function ->
                        ProcessableFunction.fromDeclaration(
                            function,
                            configuration,
                            typeParameterResolver,
                            scopeDeclaration,
                        )
                    }
                    .toList()

                ProcessableType(
                    declaration = this,
                    sourceClassName = sourceClassName,
                    wrapperClassName = wrapperClassName,
                    properties = properties,
                    functions = functions,
                    scopeDeclaration = scopeDeclaration,
                )
            }
        }

        fun fromResolver(
            resolver: Resolver,
            parentConfiguration: SourceConfiguration,
        ): Sequence<ProcessableType> {
            // Default Scope Declaration
            val defaultScopeDeclaration = DefaultKontinuityScopeDeclaration.fromResolver(resolver)

            return resolver
                .getSymbolsWithAnnotation(KONTINUITY_ANNOTATION.canonicalName)
                .mapNotNull { it as? KSClassDeclaration }
                .mapNotNull { fromDeclaration(it, parentConfiguration, defaultScopeDeclaration) }
        }
    }
}