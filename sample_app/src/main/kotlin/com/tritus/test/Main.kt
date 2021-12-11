package com.tritus.test

import com.tritus.test.ProviderGenerationTest.testCreationOfData
import com.tritus.test.ProviderGenerationTest.testMutabilityOfData
import com.tritus.test.ProviderGenerationTest.testPersistanceOfData

fun main() {
    testCreationOfData()
    testPersistanceOfData()
    testMutabilityOfData()
}