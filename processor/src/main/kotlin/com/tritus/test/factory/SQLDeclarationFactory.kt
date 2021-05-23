package com.tritus.test.factory

import com.tritus.test.model.PersistentDataDefinition
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
    """.trimLines()

    private fun createGetRecordBlock(definition: PersistentDataDefinition): String = """
        getRecord:
        SELECT * FROM ${definition.dataHolderClassName} WHERE ${definition.idProperty.name} = ?;
    """.trimLines()

    private fun createGetLastRecordBlock(definition: PersistentDataDefinition): String = """
        getLastRecord:
        SELECT * FROM ${definition.dataHolderClassName} ORDER BY ${definition.idProperty.name} DESC LIMIT 1;
    """.trimLines()

    private fun createTableCreationBlock(definition: PersistentDataDefinition): String = """
        CREATE TABLE ${definition.dataHolderClassName} (
            ${createAttributesDefinitionBlock(definition)}
        );
    """.trimLines()

    private fun createAttributesDefinitionBlock(definition: PersistentDataDefinition): String {
        val idDefinition =
            "${definition.idProperty.name} ${definition.idProperty.sqlTypeName} PRIMARY KEY AUTOINCREMENT"
        val propertyDefinitions = (definition.allProperties - definition.idProperty).map { property ->
            "${property.name} ${property.sqlTypeName}"
        }
        return (listOf(idDefinition) + propertyDefinitions).joinToString(",\n")
    }

    private fun createInsertMethodBlock(definition: PersistentDataDefinition): String = """
        createNew:
        INSERT INTO ${definition.dataHolderClassName}(${definition.dataProperties.joinToString { it.name }})
        VALUES (${definition.dataProperties.joinToString { "?" }});
    """.trimLines()
}
