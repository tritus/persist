package com.tritus.test

import com.tritus.test.ProviderGenerationTest.testAllTypesOfData
import com.tritus.test.ProviderGenerationTest.testCreationOfData
import com.tritus.test.ProviderGenerationTest.testDataWithListOfPrimitives
import com.tritus.test.ProviderGenerationTest.testDataWithListOfReferences
import com.tritus.test.ProviderGenerationTest.testMutabilityOfData
import com.tritus.test.ProviderGenerationTest.testObservabilityOfData
import com.tritus.test.ProviderGenerationTest.testObservabilityOfListOfPrimitives
import com.tritus.test.ProviderGenerationTest.testObservabilityOfListOfReferences
import com.tritus.test.ProviderGenerationTest.testObservabilityOfProperties
import com.tritus.test.ProviderGenerationTest.testObservabilityRedundancy
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
    testObservabilityRedundancy()
    testDataWithListOfPrimitives()
    testDataWithListOfReferences()
    testObservabilityOfListOfReferences()
    testObservabilityOfListOfPrimitives()
}
