package com.tritus.persist.factory

import com.tritus.persist.model.PersistentDataDefinition

internal object SQLDeclarationFactory {
    fun create(definition: PersistentDataDefinition) {
        definition.sqlDefinitionFile.parentFile.mkdirs()
        definition.sqlDefinitionFile.writeBytes(createSqlDefinitionFileContent(definition).toByteArray())
    }

    private fun createSqlDefinitionFileContent(definition: PersistentDataDefinition): String = """
${createTableCreationBlock(definition)}

${createInsertMethodBlock(definition)}

${createGetLastRecordBlock(definition)}
    """.trimIndent()

    private fun createGetLastRecordBlock(definition: PersistentDataDefinition): String = """
getLastRecord:
SELECT * FROM ${definition.dataHolderClassName} ORDER BY ${definition.idProperty.name} DESC LIMIT 1;
    """

    private fun createTableCreationBlock(definition: PersistentDataDefinition): String = """
CREATE TABLE ${definition.dataHolderClassName} (
${createAttributesDefinitionBlock(definition)}
);
    """

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
    """
}
