package com.tritus.test.utils

internal fun String.trimLines() = split("\n").joinToString("\n") { it.trim() }