package io.mockative.kontinuity

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import io.mockative.kontinuity.configuration.SourceConfiguration

data class ProcessableFile(
    val declaration: KSFile,
    val packageName: String,
    val fileName: String,
    val types: List<ProcessableType>,
) {
    companion object {
        fun fromResolver(
            resolver: Resolver,
            parentConfiguration: SourceConfiguration,
        ): List<ProcessableFile> {
            return ProcessableType.fromResolver(resolver, parentConfiguration)
                .groupBy { it.declaration.containingFile!! }
                .map { (file, types) ->
                    ProcessableFile(
                        declaration = file,
                        packageName = file.packageName.asString(),
                        fileName = file.fileName,
                        types = types,
                    )
                }
        }
    }
}