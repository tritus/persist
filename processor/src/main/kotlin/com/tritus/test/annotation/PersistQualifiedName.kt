package com.tritus.test.annotation

val persistQualifiedName = Persist::class.qualifiedName
    ?: throw IllegalArgumentException("Persist annotation should have a qualified name")
