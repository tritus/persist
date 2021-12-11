package com.tritus.test.factory

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.squareup.kotlinpoet.*
import com.tritus.test.model.PersistentDataDefinition
import com.tritus.test.model.PersistentPropertyDefinition
import com.tritus.test.utils.trimLines

internal object DataProviderFactory {
    fun create(codeGenerator: CodeGenerator, definition: PersistentDataDefinition) {
        val fileName = definition.providerClassName
        val packageName = definition.packageName
        val fileSpec = FileSpec.builder(packageName, fileName)
            .addType(createProviderClass(definition))
            .addImport(PersistDatabaseProviderFactory.databasePackage, PersistDatabaseProviderFactory.classSimpleName)
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
        return classBuilder.build()
    }

    private fun createDataholderAdapterFunSpec(definition: PersistentDataDefinition) = FunSpec
        .builder("toInterface")
        .addModifiers(KModifier.PRIVATE)
        .receiver(ClassName(definition.packageName, definition.dataHolderClassName))
        .returns(definition.className)
        .addCode(
            """
            return object : ${definition.simpleName} {
            ${definition.allProperties.joinToString("\n") { "override val ${it.name} = this@toInterface.${it.name}" }}
            }
            """.trimLines()
        )
        .build()

    private fun createRetrieveFunSpec(definition: PersistentDataDefinition) = FunSpec.builder("retrieve")
        .addParameter("id", Long::class)
        .addCode(
            """
            return object : ${definition.simpleName} {
            override val ${definition.idProperty.name} = id
            ${createInterfaceProperties(definition)}
            }
            """.trimIndent()
        )
        .returns(definition.className)
        .build()

    private fun createInterfaceProperties(definition: PersistentDataDefinition): String = definition
        .dataProperties
        .joinToString("\n") {
            if (it.isMutable) createMutableInterfaceProperty(it, definition) else createNonMutableInterfaceProperty(it, definition)
        }

    private fun createNonMutableInterfaceProperty(
        property: PersistentPropertyDefinition,
        definition: PersistentDataDefinition
    ) = """
        override val ${property.name}: ${property.typeName}
        ${createPropertyGetter(property, definition)}
    """.trimIndent()

    private fun createPropertyGetter(
        property: PersistentPropertyDefinition,
        definition: PersistentDataDefinition
    ): String = """
        get() = ${PersistDatabaseProviderFactory.classSimpleName}.getDatabase().${definition.databaseQueriesMethodName}.${property.getterMethodName}(id).executeAsOne()${if (property.typeName.isNullable) ".${property.name}" else ""}
    """.trimIndent()

    private fun createMutableInterfaceProperty(
        property: PersistentPropertyDefinition,
        definition: PersistentDataDefinition
    ) = """
        override var ${property.name}: ${property.typeName}
        ${createPropertyGetter(property, definition)}
        set(value) = ${PersistDatabaseProviderFactory.classSimpleName}.getDatabase().${definition.databaseQueriesMethodName}.${property.setterMethodName}(value, id)
    """.trimIndent()

    private fun createNewFunSpec(definition: PersistentDataDefinition): FunSpec {
        val newBuilder = FunSpec.builder("new")
        definition.dataProperties.forEach { property ->
            newBuilder.addParameter(property.name, property.typeName)
        }
        newBuilder.returns(definition.className)
        newBuilder.addCode(
            """
            val database = ${PersistDatabaseProviderFactory.classSimpleName}.getDatabase()
            val rawData = database.${definition.databaseQueriesMethodName}.transactionWithResult<${definition.dataHolderClassName}> {
                database.${definition.databaseQueriesMethodName}.createNew(${definition.dataProperties.joinToString { it.name }})
                database.${definition.databaseQueriesMethodName}.getLastRecord().executeAsOne()
            }
            return retrieve(rawData.id)
            """.trimIndent()
        )
        return newBuilder.build()
    }
}