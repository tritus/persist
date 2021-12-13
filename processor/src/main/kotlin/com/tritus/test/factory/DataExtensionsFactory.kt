package com.tritus.test.factory

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.tritus.test.model.PersistentDataDefinition
import com.tritus.test.model.PersistentPropertyDefinition

internal object DataExtensionsFactory {
    fun create(
        environment: SymbolProcessorEnvironment,
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
            .addImport("kotlinx.coroutines.flow", "distinctUntilChanged")
            .build()
        environment.codeGenerator.createNewFile(
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
            .addFunction(createRetrieveFunSpec(definition, allDefinitions))
            .let { builder ->
                definition.dataProperties
                    .filter { it.isMutable }
                    .fold(builder) { funSpec, property ->
                        funSpec.addFunction(createPropertyAsFlowFunSpec(property, definition, allDefinitions))
                    }
            }

    private fun createPropertyAsFlowFunSpec(
        property: PersistentPropertyDefinition,
        definition: PersistentDataDefinition,
        allDefinitions: Sequence<PersistentDataDefinition>
    ) = FunSpec.builder("${property.name}AsFlow")
        .receiver(definition.className)
        .returns(ClassName("kotlinx.coroutines.flow", "Flow").parameterizedBy(property.typeName))
        .addCode(
            """
                return ${PersistDatabaseProviderFactory.classSimpleName}
                    .getDatabase()
                    .${definition.databaseQueriesMethodName}
                    .${property.getterMethodName}(id)
                    .asFlow()
                    .map { ${
                if (property.isARelationship) {
                    "${property.typeName}(it.executeAsOne())"
                } else if (property.typeName.isNullable) {
                    "it.executeAsOne().${property.name}"
                } else {
                    "it.executeAsOne()"
                }
            } }
                    .distinctUntilChanged(${
                if (property.isARelationship) {
                    val idName = property.extractIdName(definition, allDefinitions)
                    "{ old, new -> old.$idName == new.$idName }"
                } else {
                    ""
                }
            })
            """.trimIndent()
        )
        .build()

    private fun createAsFlowFunSpec(definition: PersistentDataDefinition) = FunSpec.builder("asFlow")
        .receiver(definition.className)
        .returns(ClassName("kotlinx.coroutines.flow", "Flow").parameterizedBy(definition.className))
        .addCode(
            """
                return ${PersistDatabaseProviderFactory.classSimpleName}
                    .getDatabase()
                    .${definition.databaseQueriesMethodName}
                    .getRecord(id)
                    .asFlow()
                    .map { ${definition.simpleName}(id) }
            """.trimIndent()
        )
        .build()

    private fun createRetrieveFunSpec(
        definition: PersistentDataDefinition,
        allDefinitions: Sequence<PersistentDataDefinition>
    ) =
        FunSpec.builder(definition.simpleName)
            .addParameter("id", Long::class)
            .addCode(
                """
                    return object : ${definition.simpleName} {
                        override val ${definition.idProperty.name} = id
                        ${createInterfaceProperties(definition, allDefinitions)}
                    }
                """.trimIndent()
            )
            .returns(definition.className)
            .build()

    private fun createInterfaceProperties(
        definition: PersistentDataDefinition,
        allDefinitions: Sequence<PersistentDataDefinition>
    ): String = definition
        .dataProperties
        .joinToString("\n") {
            if (it.isMutable) {
                createMutableInterfaceProperty(it, definition, allDefinitions)
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
        if (property.isARelationship) {
            ".let { ${property.typeName}(it) }"
        } else {
            ""
        }
    }
    """.trimIndent()

    private fun createMutableInterfaceProperty(
        property: PersistentPropertyDefinition,
        definition: PersistentDataDefinition,
        allDefinitions: Sequence<PersistentDataDefinition>
    ) = """
        override var ${property.name}: ${property.typeName}
        ${createPropertyGetter(property, definition)}
        set(value) = ${PersistDatabaseProviderFactory.classSimpleName}
            .getDatabase()
            .${definition.databaseQueriesMethodName}
            .${property.setterMethodName}(value${
        if (property.isARelationship) {
            ".${property.extractIdName(definition, allDefinitions)}"
        } else {
            ""
        }
    }, id)
    """.trimIndent()

    private fun PersistentPropertyDefinition.extractIdName(
        definition: PersistentDataDefinition,
        allDefinitions: Sequence<PersistentDataDefinition>
    ) = allDefinitions
        .find { it.className == typeName }
        ?.idProperty
        ?.name
        .let { idName ->
            require(idName != null) {
                """
                    Could not find data type of $name defined in ${definition.containingFile}.
                    Please be sure that $typeName is annotated with @Persist
                """.trimIndent()
            }
            idName
        }

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
                if (property.isARelationship) {
                    "${property.name}.${property.extractIdName(definition, allDefinitions)}"
                } else {
                    property.name
                }
            }
        newBuilder.addCode(
            """
            val database = ${PersistDatabaseProviderFactory.classSimpleName}
                .getDatabase()
            val rawData = database
                .${definition.databaseQueriesMethodName}
                .transactionWithResult<${definition.dataHolderClassName}> {
                    database
                        .${definition.databaseQueriesMethodName}
                        .createNew($realArgumentsList)
                    database
                        .${definition.databaseQueriesMethodName}
                        .getLastRecord()
                        .executeAsOne()
                }
            return ${definition.simpleName}(rawData.id)
            """.trimIndent()
        )
        return newBuilder.build()
    }
}