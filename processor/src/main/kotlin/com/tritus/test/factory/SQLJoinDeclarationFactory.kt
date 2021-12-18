package com.tritus.test.factory

import com.tritus.test.model.PersistentPropertyDefinition
import com.tritus.test.utils.trimLines

internal object SQLJoinDeclarationFactory {
    const val REFERENCE_ID_COLUMN_NAME = "ref_id"
    const val VALUE_COLUMN_NAME = "value"

    fun create(propertyDefinition: PersistentPropertyDefinition.List) {
        propertyDefinition.sqlDefinitionFile.parentFile.mkdirs()
        propertyDefinition.sqlDefinitionFile.writeBytes(createSqlDefinitionFileContent(propertyDefinition).toByteArray())
    }

    private fun createSqlDefinitionFileContent(propertyDefinition: PersistentPropertyDefinition.List): String = """
        CREATE TABLE ${propertyDefinition.dataholderClassName} (
            id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
            $REFERENCE_ID_COLUMN_NAME INTEGER NOT NULL,
            $VALUE_COLUMN_NAME ${propertyDefinition.itemTypeDefinition.sqlTypeName} NOT NULL
        );
    """.trimLines()
}