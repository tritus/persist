package com.tritus.persist.adapter

import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.ClassName
import com.tritus.persist.model.PersistentPropertyDefinition

internal object PersistentPropertyDefinitionAdapter {
    fun KSPropertyDeclaration.toPersistentPropertyDefinition() : PersistentPropertyDefinition {
        val name = simpleName.asString()
        val parameterTypeDeclaration = type.resolve().declaration

        return PersistentPropertyDefinition(
            name,
            ClassName(
                parameterTypeDeclaration.packageName.asString(),
                parameterTypeDeclaration.simpleName.asString()
            )
        )
    }
}