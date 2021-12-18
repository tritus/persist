package com.tritus.test.factory

import com.tritus.test.model.PersistentDataDefinition
import com.tritus.test.model.PersistentPropertyDefinition
import com.tritus.test.utils.trimLines

internal object SQLDeclarationFactory {
    fun create(definition: PersistentDataDefinition) {
        definition.sqlDefinitionFile.parentFile.mkdirs()
        definition.sqlDefinitionFile.writeBytes(createSqlDefinitionFileContent(definition).toByteArray())
    }

    private fun createSqlDefinitionFileContent(definition: PersistentDataDefinition): String = """
        ${createTableCreationBlock(definition)}
        
        ${createInsertMethodBlock(definition)}
        
        ${createGetLastRecordBlock(definition)}
        
        ${createGetRecordBlock(definition)}
        
        ${createGettersBlock(definition)}
        
        ${createSettersBlock(definition)}
    """.trimLines()

    private fun createGetRecordBlock(definition: PersistentDataDefinition): String = """
        getRecord:
        SELECT * FROM ${definition.dataholderClassName} WHERE ${definition.idProperty.name} = ?;
    """.trimLines()

    private fun createGetLastRecordBlock(definition: PersistentDataDefinition): String = """
        getLastRecord:
        SELECT * FROM ${definition.dataholderClassName} ORDER BY ${definition.idProperty.name} DESC LIMIT 1;
    """.trimLines()

    private fun createTableCreationBlock(definition: PersistentDataDefinition): String = """
        CREATE TABLE ${definition.dataholderClassName} (
            ${createAttributesDefinitionBlock(definition)}
        );
    """.trimLines()

    private fun createAttributesDefinitionBlock(definition: PersistentDataDefinition): String {
        val idDefinition =
            "${definition.idProperty.name} ${definition.idProperty.typeDefinition.sqlTypeName} PRIMARY KEY AUTOINCREMENT"
        val propertyDefinitions = definition.dataProperties
            .filterIsInstance<PersistentPropertyDefinition.Primitive>()
            .map { property -> "${property.name} ${property.typeDefinition.sqlTypeName}" }
        return (listOf(idDefinition) + propertyDefinitions).joinToString(",\n")
    }

    private fun createInsertMethodBlock(definition: PersistentDataDefinition): String = """
        createNew:
        INSERT INTO ${definition.dataholderClassName}(${
        definition.dataProperties.filterIsInstance<PersistentPropertyDefinition.Primitive>().joinToString { it.name }
    })
        VALUES (${
        definition.dataProperties.filterIsInstance<PersistentPropertyDefinition.Primitive>().joinToString { "?" }
    });
    """.trimLines()

    private fun createGettersBlock(definition: PersistentDataDefinition): String = definition
        .dataProperties
        .joinToString("\n\n") { createGetterBlock(it, definition) }

    private fun createGetterBlock(
        propertyDefinition: PersistentPropertyDefinition,
        dataDefinition: PersistentDataDefinition
    ): String = when (propertyDefinition) {
        is PersistentPropertyDefinition.List -> createListGetterBlock(propertyDefinition)
        is PersistentPropertyDefinition.Primitive -> createPrimitiveGetterBlock(propertyDefinition, dataDefinition)
    }

    private fun createListGetterBlock(propertyDefinition: PersistentPropertyDefinition.List) = """
        ${propertyDefinition.getterMethodName}:
        SELECT ${SQLJoinDeclarationFactory.VALUE_COLUMN_NAME} FROM ${propertyDefinition.dataholderClassName} WHERE ${SQLJoinDeclarationFactory.REFERENCE_ID_COLUMN_NAME} = ?;
    """.trimLines()

    private fun createPrimitiveGetterBlock(
        propertyDefinition: PersistentPropertyDefinition.Primitive,
        dataDefinition: PersistentDataDefinition
    ) = """
        ${propertyDefinition.getterMethodName}:
        SELECT ${propertyDefinition.name} FROM ${dataDefinition.dataholderClassName} WHERE ${dataDefinition.idProperty.name} = ?;
    """.trimLines()

    private fun createSettersBlock(definition: PersistentDataDefinition): String = definition
        .dataProperties
        .joinToString("\n\n") { createSetterBlock(it, definition) }

    private fun createSetterBlock(
        propertyDefinition: PersistentPropertyDefinition,
        dataDefinition: PersistentDataDefinition
    ): String = when (propertyDefinition) {
        is PersistentPropertyDefinition.List -> """
            ${createListItemDeleteBlock(propertyDefinition)}
            ${createListItemSetterBlock(propertyDefinition)}
        """.trimIndent()
        is PersistentPropertyDefinition.Primitive -> createPrimitiveSetterBlock(propertyDefinition, dataDefinition)
    }

    private fun createListItemDeleteBlock(propertyDefinition: PersistentPropertyDefinition.List) = """
        ${propertyDefinition.deleteMethodName}:
        DELETE FROM ${propertyDefinition.dataholderClassName} WHERE ${SQLJoinDeclarationFactory.REFERENCE_ID_COLUMN_NAME} = ?;
    """.trimLines()

    private fun createListItemSetterBlock(propertyDefinition: PersistentPropertyDefinition.List) = """
        ${propertyDefinition.itemSetterMethodName}:
        INSERT INTO ${propertyDefinition.dataholderClassName}(${SQLJoinDeclarationFactory.REFERENCE_ID_COLUMN_NAME}, ${SQLJoinDeclarationFactory.VALUE_COLUMN_NAME}) VALUES (?, ?);
    """.trimLines()

    private fun createPrimitiveSetterBlock(
        propertyDefinition: PersistentPropertyDefinition.Primitive,
        dataDefinition: PersistentDataDefinition
    ) = """
        ${propertyDefinition.setterMethodName}:
        UPDATE ${dataDefinition.dataholderClassName} SET ${propertyDefinition.name} = ? WHERE ${dataDefinition.idProperty.name} = ?;
    """.trimLines()
}
