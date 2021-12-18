package com.tritus.test.annotation

val persistentIdQualifiedName = PersistentId::class.qualifiedName
    ?: throw IllegalArgumentException("PersistentId annotation should have a qualified name")
