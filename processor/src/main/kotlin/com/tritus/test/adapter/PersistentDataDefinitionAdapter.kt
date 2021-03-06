package com.tritus.test.adapter

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.ClassName
import com.tritus.test.adapter.PersistentPropertyDefinitionAdapter.toPersistentPropertyDefinition
import com.tritus.test.adapter.PersistentPropertyDefinitionAdapter.toPrimitivePropertyDefinition
import com.tritus.test.annotation.persistentIdQualifiedName
import com.tritus.test.model.PersistentDataDefinition
import com.tritus.test.model.PersistentPropertyDefinition
import java.io.File

internal object PersistentDataDefinitionAdapter {
    fun KSClassDeclaration.toPersistentDataDefinition(persistAnnotatedSymbols: Sequence<KSClassDeclaration>): PersistentDataDefinition {
        val simpleNameString = simpleName.asString()
        val dataHolderClassName = "${simpleNameString}_Data"
        val packageNameString = packageName.asString()
        val idProperty = extractIdProperty(persistAnnotatedSymbols)
        val pathInSource = packageNameString.replace(".", "/")
        val containingClassFile = containingFile
        require(containingClassFile != null) { "Containing file of ${qualifiedName?.asString()} should not be null" }
        val sqlDefinitionFileName = "$dataHolderClassName.sq"
        val sqlDefinitionsPath = containingClassFile.filePath
            .replace(pathInSource, "")
            .split("/")
            .dropLast(3)
            .plus("sqldelight")
            .joinToString("/")
        val dataProperties = getAllProperties()
            .filter { it.simpleName.asString() != idProperty.name }
            .map { it.toPersistentPropertyDefinition(this, persistAnnotatedSymbols) }
            .toList()
        return PersistentDataDefinition(
            simpleName = simpleNameString,
            dataholderClassName = dataHolderClassName,
            databaseQueriesMethodName = "${dataHolderClassName.replace(Regex("^.")) { it.value.lowercase() }}Queries",
            extensionsFileName = "${simpleNameString}Extensions",
            packageName = packageNameString,
            className = ClassName(packageNameString, simpleNameString),
            containingFile = containingClassFile,
            idProperty = idProperty,
            dataProperties = dataProperties,
            sqlDelightFolder = File(sqlDefinitionsPath),
            sqlDefinitionFile = File(sqlDefinitionsFolderPath(), sqlDefinitionFileName)
        )
    }

    fun KSClassDeclaration.sqlDefinitionsFolderPath(): File {
        return File(sqldelightFolder(), pathInSource())
    }

    private fun KSClassDeclaration.sqldelightFolder(): File {
        val containingClassFile = containingFile
        require(containingClassFile != null) { "Containing file of ${qualifiedName?.asString()} should not be null" }
        return File(
            containingClassFile.filePath
                .replace(pathInSource(), "")
                .split("/")
                .dropLast(3)
                .plus("sqldelight")
                .joinToString("/")
        )
    }

    private fun KSClassDeclaration.pathInSource(): String = packageName.asString().replace(".", "/")

    private fun KSClassDeclaration.extractIdProperty(persistAnnotatedSymbols: Sequence<KSClassDeclaration>): PersistentPropertyDefinition.Primitive {
        val idProperties = getAllProperties().filter { property ->
            property.annotations.any { annotationSymbol ->
                val annotation = annotationSymbol.annotationType.resolve().declaration.qualifiedName?.asString()
                annotation == persistentIdQualifiedName
            }
        }
        val idPropertiesCount = idProperties.count()
        require(idPropertiesCount > 0) {
            """
                No id property found on ${qualifiedName?.asString()}.
                There should be at least one property annotated with @PersistentId.
                This property should be of type Long and should be a val.
                It will enable you to retrieve the persisted data later on.
            """.trimIndent()
        }
        require(idPropertiesCount < 2) {
            """
                Too many id properties found on ${qualifiedName?.asString()}.
                There should be no more than one persistent id per persisted object.
                Current id declarations on ${simpleName.asString()} properties [${
            idProperties.map { it.simpleName.asString() }.joinToString()
            }]
            """.trimIndent()
        }
        val idProperty = idProperties.first()
        require(!idProperty.isMutable) {
            """
                Mutable id found on ${qualifiedName?.asString()}.
                The property annotated with @PersistentId should not be mutable.
            """.trimIndent()
        }
        require(idProperty.type.resolve().declaration.qualifiedName?.asString() == Long::class.qualifiedName) {
            """
                Property ${idProperty.simpleName.asString()} declared in ${qualifiedName?.asString()}.
                The property annotated with @PersistentId must be of type Long.
            """.trimIndent()
        }
        return idProperty.toPrimitivePropertyDefinition(persistAnnotatedSymbols)
    }
}
