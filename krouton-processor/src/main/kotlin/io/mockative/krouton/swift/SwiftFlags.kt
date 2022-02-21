package io.mockative.krouton.swift

import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.INT32
import io.outfoxx.swiftpoet.INT64

class SwiftFlags(
    val moduleName: String,
    val outputDir: String?,
    val generateAsyncExtensions: Boolean?,
    val intType: DeclaredTypeName,
    val longType: DeclaredTypeName,
) {
    companion object {
        lateinit var shared: SwiftFlags

        fun fromOptions(options: Map<String, String>): SwiftFlags {
            val moduleName: String = options["krouton.swift.moduleName"] ?: "shared"
            val outputDir: String? = options["krouton.swift.outputDir"]
            val generateAsyncExtensions: Boolean? = options["krouton.swift.generateAsyncExtensions"]?.toBooleanStrict()
            val intType = options["krouton.swift.intType"]?.let { DeclaredTypeName.typeName(it) } ?: INT32
            val longType = options["krouton.swift.longType"]?.let { DeclaredTypeName.typeName(it) } ?: INT64

            return SwiftFlags(
                moduleName = moduleName,
                outputDir = outputDir,
                generateAsyncExtensions = generateAsyncExtensions,
                intType = intType,
                longType = longType,
            )
        }
    }
}