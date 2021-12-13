package com.tritus.test.factory

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.tritus.test.model.PersistentDataDefinition
import com.tritus.test.model.PersistentPropertyDefinition

internal object DataExtensionsFactory {
    fun create(
        codeGenerator: CodeGenerator,
        definition: PersistentDataDefinition,
        allDefinitions: Sequence<PersistentDataDefinition>
    ) {
        val fileName = definition.extensionsFileName
        val packageName = definition.packageName
        val fileSpec = FileSpec.builder(packageName, fileName)
            .addExtensionFunctions(definition, allDefinitions)
            .addImport(PersistDatabaseProviderFactory.databasePackage, PersistDatabaseProviderFactory.classSimpleName)
            .addImport("com.squareup.sqldelight.runtime.coroutines", "asFlow")
            .addImport("kotlinx.coroutines.flow", "map")
            .build()
        codeGenerator.createNewFile(
            Dependencies(true, definition.containingFile),
            packageName,
            fileName
        ).use { dataHolderFile -> dataHolderFile.write(fileSpec.toString().toByteArray()) }
    }

    private fun FileSpec.Builder.addExtensionFunctions(
        definition: PersistentDataDefinition,
        allDefinitions: Sequence<PersistentDataDefinition>
    ) =
        addFunction(createAsFlowFunSpec(definition))
            .addFunction(createNewFunSpec(definition, allDefinitions))
            .addFunction(createRetrieveFunSpec(definition))
            .let { builder ->
                definition.dataProperties
                    .filter { it.isMutable }
                    .fold(builder) { funSpec, property ->
                        funSpec.addFunction(createPropertyAsFlowFunSpec(property, definition))
                    }
            }

    private fun createPropertyAsFlowFunSpec(
        property: PersistentPropertyDefinition,
        definition: PersistentDataDefinition
    ) = FunSpec.builder("${property.name}AsFlow")
        .receiver(definition.className)
        .returns(ClassName("kotlinx.coroutines.flow", "Flow").parameterizedBy(property.typeName))
        .addCode(
            """
                return ${PersistDatabaseProviderFactory.classSimpleName}.getDatabase().${definition.databaseQueriesMethodName}.${property.getterMethodName}(id).asFlow().map { it.executeAsOne() }
            """.trimIndent()
        )
        .build()

    private fun createAsFlowFunSpec(definition: PersistentDataDefinition) = FunSpec.builder("asFlow")
        .receiver(definition.className)
        .returns(ClassName("kotlinx.coroutines.flow", "Flow").parameterizedBy(definition.className))
        .addCode(
            """
                return ${PersistDatabaseProviderFactory.classSimpleName}.getDatabase().${definition.databaseQueriesMethodName}.getRecord(id).asFlow().map { ${definition.simpleName}(id) }
            """.trimIndent()
        )
        .build()

    private fun createRetrieveFunSpec(definition: PersistentDataDefinition) =
        FunSpec.builder(definition.simpleName)
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
            if (it.isMutable) {
                createMutableInterfaceProperty(it, definition)
            } else {
                createNonMutableInterfaceProperty(it, definition)
            }
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
        get() = ${PersistDatabaseProviderFactory.classSimpleName}
            .getDatabase()
            .${definition.databaseQueriesMethodName}
            .${property.getterMethodName}(id)
            .executeAsOne()${
                if (property.typeName.isNullable) {
                    ".${property.name}"
                } else {
                    ""
                }
            }${ 
                if (property.isARelationShip) {
                    ".let { ${property.typeName}(it) }"
                } else {
                    ""
                }
            }
    """.trimIndent()

    private fun createMutableInterfaceProperty(
        property: PersistentPropertyDefinition,
        definition: PersistentDataDefinition
    ) = """
        override var ${property.name}: ${property.typeName}
        ${createPropertyGetter(property, definition)}
        set(value) = ${PersistDatabaseProviderFactory.classSimpleName}.getDatabase().${definition.databaseQueriesMethodName}.${property.setterMethodName}(value, id)
    """.trimIndent()

    private fun createNewFunSpec(
        definition: PersistentDataDefinition,
        allDefinitions: Sequence<PersistentDataDefinition>
    ): FunSpec {
        val newBuilder = FunSpec.builder(definition.simpleName)
        definition.dataProperties.forEach { property ->
            newBuilder.addParameter(property.name, property.typeName)
        }
        newBuilder.returns(definition.className)
        val realArgumentsList = definition.dataProperties
            .joinToString { property ->
                if (property.isARelationShip) {
                    val idName = allDefinitions.find { it.className == property.typeName }?.idProperty?.name
                    require(idName != null) {
                        """
                            Could not find data type of ${property.name} defined in ${definition.containingFile}.
                            Please be sure that ${property.typeName} is annotated with @Persist
                        """.trimIndent()
                    }
                    "${property.name}.$idName"
                } else {
                    property.name
                }
            }
        newBuilder.addCode(
            """
            val database = ${PersistDatabaseProviderFactory.classSimpleName}.getDatabase()
            val rawData = database.${definition.databaseQueriesMethodName}.transactionWithResult<${definition.dataHolderClassName}> {
                database.${definition.databaseQueriesMethodName}.createNew($realArgumentsList)
                database.${definition.databaseQueriesMethodName}.getLastRecord().executeAsOne()
            }
            return ${definition.simpleName}(rawData.id)
            """.trimIndent()
        )
        return newBuilder.build()
    }
}