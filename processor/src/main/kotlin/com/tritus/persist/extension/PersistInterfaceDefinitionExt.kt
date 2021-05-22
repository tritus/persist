package com.tritus.persist.extension

import com.google.devtools.ksp.symbol.KSClassDeclaration

internal fun KSClassDeclaration.getDataHolderClassName() = "${simpleName.asString()}_Data"

internal fun KSClassDeclaration.getProviderClassName() = "${simpleName.asString()}Provider"