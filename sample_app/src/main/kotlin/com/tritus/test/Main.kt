package com.tritus.test

import com.tritus.test.ProviderGenerationTest.testAllTypesOfData
import com.tritus.test.ProviderGenerationTest.testCreationOfData
import com.tritus.test.ProviderGenerationTest.testMutabilityOfData
import com.tritus.test.ProviderGenerationTest.testObservabilityOfData
import com.tritus.test.ProviderGenerationTest.testObservabilityOfProperties
import com.tritus.test.ProviderGenerationTest.testPersistanceOfData
import com.tritus.test.ProviderGenerationTest.testRelationBetweenPersistedData
import com.tritus.test.ProviderGenerationTest.testRelationObservability

fun main() {
    testCreationOfData()
    testPersistanceOfData()
    testMutabilityOfData()
    testAllTypesOfData()
    testObservabilityOfData()
    testObservabilityOfProperties()
    testRelationBetweenPersistedData()
    testRelationObservability()
}
