package io.mockative.krouton.swift

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import io.outfoxx.swiftpoet.FileSpec
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

fun FileSpec.writeTo(
    codeGenerator: CodeGenerator,
    packageName: String,
    dependencies: Dependencies
) {
    val file = codeGenerator.createNewFile(dependencies, packageName, name, "swift")
    // Don't use writeTo(file) because that tries to handle directories under the hood
    OutputStreamWriter(file, StandardCharsets.UTF_8)
        .use(::writeTo)
}