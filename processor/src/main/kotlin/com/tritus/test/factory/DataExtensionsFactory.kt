package com.tritus.test.factory

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.plusParameter
import com.tritus.test.model.PersistentDataDefinition
import com.tritus.test.model.PersistentPropertyDefinition
import com.tritus.test.model.PropertyTypeDefinition

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
            .addImport("kotlinx.coroutines.flow", "combine")
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
    ): FunSpec = when (property) {
        is PersistentPropertyDefinition.List -> createListPropertyAsFlowFunSpec(property, definition, allDefinitions)
        is PersistentPropertyDefinition.Primitive -> createPrimitivePropertyAsFlowFunSpec(
            property,
            definition,
            allDefinitions
        )
    }

    private fun createListPropertyAsFlowFunSpec(
        property: PersistentPropertyDefinition.List,
        definition: PersistentDataDefinition,
        allDefinitions: Sequence<PersistentDataDefinition>
    ) = FunSpec.builder("${property.name}AsFlow")
        .receiver(definition.className)
        .returns(
            ClassName("kotlinx.coroutines.flow", "Flow")
                .parameterizedBy(property.interfaceTypeName())
        )
        .addCode(
            """
                return ${PersistDatabaseProviderFactory.classSimpleName}
                    .getDatabase()
                    .${definition.databaseQueriesMethodName}
                    .${property.getterMethodName}(${definition.idProperty.name})
                    .asFlow()
                    .map { newItemsQuery ->
                        val newItems = newItemsQuery.executeAsList()
                        ${
            if (property.itemTypeDefinition.isARelationship) {
                "newItems.map { ${property.itemTypeDefinition.typeName}(it) }"
            } else if (property.itemTypeDefinition.typeName.isNullable) {
                "newItems.map { it.${property.name} }"
            } else {
                "newItems"
            }
            } 
                    }
                    .distinctUntilChanged(${
            if (property.itemTypeDefinition.isARelationship) {
                val idName = property.itemTypeDefinition.extractIdName(property, definition, allDefinitions)
                "{ old, new -> old.map { it.$idName } == new.map { it.$idName } }"
            } else {
                ""
            }
            })
            """.trimIndent()
        )
        .build()

    private fun createPrimitivePropertyAsFlowFunSpec(
        property: PersistentPropertyDefinition.Primitive,
        definition: PersistentDataDefinition,
        allDefinitions: Sequence<PersistentDataDefinition>
    ) = FunSpec.builder("${property.name}AsFlow")
        .receiver(definition.className)
        .returns(ClassName("kotlinx.coroutines.flow", "Flow").parameterizedBy(property.typeDefinition.typeName))
        .addCode(
            """
                return ${PersistDatabaseProviderFactory.classSimpleName}
                    .getDatabase()
                    .${definition.databaseQueriesMethodName}
                    .${property.getterMethodName}(${definition.idProperty.name})
                    .asFlow()
                    .map { ${
            if (property.typeDefinition.isARelationship) {
                "${property.typeDefinition.typeName}(it.executeAsOne())"
            } else if (property.typeDefinition.typeName.isNullable) {
                "it.executeAsOne().${property.name}"
            } else {
                "it.executeAsOne()"
            }
            } }
                    .distinctUntilChanged(${
            if (property.typeDefinition.isARelationship) {
                val idName = property.typeDefinition.extractIdName(property, definition, allDefinitions)
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
                    .getRecord(${definition.idProperty.name})
                    .asFlow()
                    ${
            definition.dataProperties
                .filterIsInstance<PersistentPropertyDefinition.List>()
                .filter { it.isMutable }
                .joinToString("\n") {
                    ".combine(${it.name}AsFlow()) { record, _ -> record }"
                }
            }
                    .map { ${definition.simpleName}(${definition.idProperty.name}) }
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
    ) = when (property) {
        is PersistentPropertyDefinition.List -> createNonMutableInterfaceListProperty(property, definition)
        is PersistentPropertyDefinition.Primitive -> createNonMutableInterfacePrimitiveProperty(property, definition)
    }

    private fun createNonMutableInterfaceListProperty(
        property: PersistentPropertyDefinition.List,
        definition: PersistentDataDefinition
    ) = """
        override val ${property.name}: List<${property.itemTypeDefinition.typeName}>
        ${createPropertyGetter(property, definition)}
    """.trimIndent()

    private fun createNonMutableInterfacePrimitiveProperty(
        property: PersistentPropertyDefinition.Primitive,
        definition: PersistentDataDefinition
    ) = """
        override val ${property.name}: ${property.typeDefinition.typeName}
        ${createPropertyGetter(property, definition)}
    """.trimIndent()

    private fun createPropertyGetter(
        property: PersistentPropertyDefinition,
        definition: PersistentDataDefinition
    ) = when (property) {
        is PersistentPropertyDefinition.List -> createListPropertyGetter(property, definition)
        is PersistentPropertyDefinition.Primitive -> createPrimitivePropertyGetter(property, definition)
    }

    private fun createListPropertyGetter(
        property: PersistentPropertyDefinition.List,
        definition: PersistentDataDefinition
    ) = """
        get() = ${PersistDatabaseProviderFactory.classSimpleName}
                    .getDatabase()
                    .${definition.databaseQueriesMethodName}
                    .${property.getterMethodName}(${definition.idProperty.name})
                    .executeAsList()${
    if (property.itemTypeDefinition.isARelationship) {
        ".map { ${property.itemTypeDefinition.typeName}(it) }"
    } else {
        ""
    }
    }
    """.trimIndent()

    private fun createPrimitivePropertyGetter(
        property: PersistentPropertyDefinition.Primitive,
        definition: PersistentDataDefinition
    ) = """
        get() = ${PersistDatabaseProviderFactory.classSimpleName}
            .getDatabase()
            .${definition.databaseQueriesMethodName}
            .${property.getterMethodName}(${definition.idProperty.name})
            .executeAsOne()${
    if (property.typeDefinition.typeName.isNullable) {
        ".${property.name}"
    } else {
        ""
    }
    }${
    if (property.typeDefinition.isARelationship) {
        ".let { ${property.typeDefinition.typeName}(it) }"
    } else {
        ""
    }
    }
    """.trimIndent()

    private fun createMutableInterfaceProperty(
        property: PersistentPropertyDefinition,
        definition: PersistentDataDefinition,
        allDefinitions: Sequence<PersistentDataDefinition>
    ) = when (property) {
        is PersistentPropertyDefinition.List -> createMutableInterfaceListProperty(property, definition, allDefinitions)
        is PersistentPropertyDefinition.Primitive -> createMutableInterfacePrimitiveProperty(
            property,
            definition,
            allDefinitions
        )
    }

    private fun createMutableInterfaceListProperty(
        property: PersistentPropertyDefinition.List,
        definition: PersistentDataDefinition,
        allDefinitions: Sequence<PersistentDataDefinition>
    ) = """
        override var ${property.name}: List<${property.itemTypeDefinition.typeName}>
        ${createPropertyGetter(property, definition)}
        set(value) {
            ${PersistDatabaseProviderFactory.classSimpleName}
                .getDatabase()
                .${definition.databaseQueriesMethodName}
                .${property.deleteMethodName}(${definition.idProperty.name})
            value.forEach { item ->
                ${PersistDatabaseProviderFactory.classSimpleName}
                    .getDatabase()
                    .${definition.databaseQueriesMethodName}
                    .${property.itemSetterMethodName}(
                        ${definition.idProperty.name},
                        item${property.valueAccessor(definition, allDefinitions)}
                    )
            }
        }
    """.trimIndent()

    private fun createMutableInterfacePrimitiveProperty(
        property: PersistentPropertyDefinition.Primitive,
        definition: PersistentDataDefinition,
        allDefinitions: Sequence<PersistentDataDefinition>
    ) = """
        override var ${property.name}: ${property.typeDefinition.typeName}
        ${createPropertyGetter(property, definition)}
        set(value) = ${PersistDatabaseProviderFactory.classSimpleName}
            .getDatabase()
            .${definition.databaseQueriesMethodName}
            .${property.setterMethodName}(value${
    if (property.typeDefinition.isARelationship) {
        ".${property.typeDefinition.extractIdName(property, definition, allDefinitions)}"
    } else {
        ""
    }
    }, ${definition.idProperty.name})
    """.trimIndent()

    private fun PropertyTypeDefinition.extractIdName(
        property: PersistentPropertyDefinition,
        definition: PersistentDataDefinition,
        allDefinitions: Sequence<PersistentDataDefinition>
    ) = allDefinitions
        .find { it.className == typeName }
        ?.idProperty
        ?.name
        .let { idName ->
            require(idName != null) {
                """
                    Could not find data type of ${property.name} defined in ${definition.containingFile}.
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
            newBuilder.addParameter(property.name, property.interfaceTypeName())
        }
        newBuilder.returns(definition.className)
        val realArgumentsList = definition.dataProperties
            .filterIsInstance<PersistentPropertyDefinition.Primitive>()
            .joinToString { property -> property.realArgument(definition, allDefinitions) }
        newBuilder.addCode(
            """
            val database = ${PersistDatabaseProviderFactory.classSimpleName}
                .getDatabase()
            val rawData = database
                .${definition.databaseQueriesMethodName}
                .transactionWithResult<${definition.dataholderClassName}> {
                    database
                        .${definition.databaseQueriesMethodName}
                        .createNew($realArgumentsList)
                    val createdEntity = database
                        .${definition.databaseQueriesMethodName}
                        .getLastRecord()
                        .executeAsOne()
                    ${createNewListProperties(definition, allDefinitions)}
                    createdEntity
                }
            return ${definition.simpleName}(rawData.${definition.idProperty.name})
            """.trimIndent()
        )
        return newBuilder.build()
    }

    private fun createNewListProperties(
        definition: PersistentDataDefinition,
        allDefinitions: Sequence<PersistentDataDefinition>
    ): String = definition.dataProperties
        .filterIsInstance<PersistentPropertyDefinition.List>()
        .joinToString("\n") { property ->
            """
                database
                    .${definition.databaseQueriesMethodName}
                    .${property.deleteMethodName}(createdEntity.${definition.idProperty.name})
                ${property.name}.forEach { item ->
                    database
                        .${definition.databaseQueriesMethodName}
                        .${property.itemSetterMethodName}(
                            createdEntity.${definition.idProperty.name}, 
                            item${property.valueAccessor(definition, allDefinitions)}
                        )
                }
            """.trimIndent()
        }

    private fun PersistentPropertyDefinition.List.valueAccessor(
        definition: PersistentDataDefinition,
        allDefinitions: Sequence<PersistentDataDefinition>
    ) = if (itemTypeDefinition.isARelationship) {
        ".${itemTypeDefinition.extractIdName(this, definition, allDefinitions)}"
    } else {
        ""
    }

    private fun PersistentPropertyDefinition.interfaceTypeName() = when (this) {
        is PersistentPropertyDefinition.List ->
            ClassName
                .bestGuess(List::class.qualifiedName!!)
                .plusParameter(itemTypeDefinition.typeName)
        is PersistentPropertyDefinition.Primitive -> typeDefinition.typeName
    }

    private fun PersistentPropertyDefinition.Primitive.realArgument(
        definition: PersistentDataDefinition,
        allDefinitions: Sequence<PersistentDataDefinition>
    ): String = if (typeDefinition.isARelationship) {
        "$name.${typeDefinition.extractIdName(this, definition, allDefinitions)}"
    } else {
        name
    }
}
