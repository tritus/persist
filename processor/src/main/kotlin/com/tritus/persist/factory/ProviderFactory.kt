package com.tritus.persist.factory

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.squareup.kotlinpoet.*
import com.tritus.persist.model.PersistentDataDefinition

internal object ProviderFactory {
    fun create(codeGenerator: CodeGenerator, definition: PersistentDataDefinition) {
        val fileName = definition.providerClassName
        val packageName = definition.packageName
        val fileSpec = FileSpec.builder(packageName, fileName)
            .addType(createProviderClass(definition))
            .build()
        codeGenerator.createNewFile(
            Dependencies(true, definition.containingFile),
            packageName,
            fileName
        ).use { dataHolderFile -> dataHolderFile.write(fileSpec.toString().toByteArray()) }
    }

    private fun createProviderClass(definition: PersistentDataDefinition): TypeSpec {
        val classBuilder = TypeSpec.Companion.objectBuilder(definition.providerClassName)
        classBuilder.addFunction(createNewFunSpec(definition))
        classBuilder.addFunction(createRetrieveFunSpec(definition))
        classBuilder.addFunction(createDataholderAdapterFunSpec(definition))
        return classBuilder.build()
    }

    private fun createDataholderAdapterFunSpec(definition: PersistentDataDefinition) = FunSpec
        .builder("toInterface")
        .addModifiers(KModifier.PRIVATE)
        .receiver(ClassName(definition.packageName, definition.dataHolderClassName))
        .returns(definition.className)
        .addCode("""
return object : ${definition.simpleName} {
${definition.allProperties.joinToString("\n") { "override val ${it.name} = this@toInterface.${it.name}" }}
}
        """)
        .build()

    private fun createRetrieveFunSpec(definition: PersistentDataDefinition) = FunSpec.builder("retrieve")
        .addParameter("id", Long::class)
        .addCode("""
            val database = PersistDatabaseProvider.getDatabase()
            return database.${definition.databaseQueriesMethodName}.getRecord(id).executeAsOne().toInterface()
        """.trimIndent())
        .returns(definition.className)
        .build()

    private fun createNewFunSpec(definition: PersistentDataDefinition): FunSpec {
        val newBuilder = FunSpec.builder("new")
        definition.dataProperties.forEach { property ->
            newBuilder.addParameter(property.name, property.className)
        }
        newBuilder.returns(definition.className)
        newBuilder.addCode(
            """
            val database = PersistDatabaseProvider.getDatabase()
            val rawData = database.${definition.databaseQueriesMethodName}.transactionWithResult<${definition.dataHolderClassName}> {
                database.${definition.databaseQueriesMethodName}.createNew(${definition.dataProperties.joinToString { it.name }})
                database.${definition.databaseQueriesMethodName}.getLastRecord().executeAsOne()
            }
            return rawData.toInterface()
            """.trimIndent()
        )
        return newBuilder.build()
    }
}