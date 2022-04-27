package io.mockative.kontinuity

import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeParameterResolver

data class ProcessableType(
    val declaration: KSClassDeclaration,
    val sourceClassName: ClassName,
    val wrapperClassName: ClassName,
    val properties: List<ProcessableProperty>,
    val functions: List<ProcessableFunction>,
) {
    companion object {
        private fun fromDeclaration(
            declaration: KSClassDeclaration,
            parentConfiguration: SourceConfiguration
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

                val properties = declaration.getAllProperties()
                    .filter { it.isPublic() }
                    .mapNotNull { property ->
                        ProcessableProperty.fromDeclaration(
                            property,
                            configuration,
                            typeParameterResolver
                        )
                    }
                    .toList()

                val functions = declaration.getAllFunctions()
                    .filter { it.isPublic() }
                    .mapNotNull { function ->
                        ProcessableFunction.fromDeclaration(
                            function,
                            configuration,
                            typeParameterResolver
                        )
                    }
                    .toList()

                ProcessableType(
                    declaration = this,
                    sourceClassName = sourceClassName,
                    wrapperClassName = wrapperClassName,
                    properties = properties,
                    functions = functions,
                )
            }
        }

        fun fromResolver(
            resolver: Resolver,
            parentConfiguration: SourceConfiguration
        ): Sequence<ProcessableType> {
            return resolver
                .getSymbolsWithAnnotation(KONTINUITY_ANNOTATION.canonicalName)
                .mapNotNull { it as? KSClassDeclaration }
                .mapNotNull { fromDeclaration(it, parentConfiguration) }
        }
    }
}