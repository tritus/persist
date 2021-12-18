package com.tritus.test

import com.tritus.test.model.DataWithAllTypes
import com.tritus.test.model.DataWithChangingRelation
import com.tritus.test.model.DataWithRelation
import com.tritus.test.model.TestData
import com.tritus.test.model.TestDataWithPrimitiveList
import com.tritus.test.model.TestDataWithPrimitiveStaticList
import com.tritus.test.model.TestDataWithRefList
import com.tritus.test.model.TestDataWithRefStaticList
import com.tritus.test.model.VariableTestData
import com.tritus.test.model.asFlow
import com.tritus.test.model.descriptionAsFlow
import com.tritus.test.model.descriptionsAsFlow
import com.tritus.test.model.nameAsFlow
import com.tritus.test.model.testDataAsFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking

object ProviderGenerationTest {

    const val OBSERVATION_DELAY = 100L

    fun testCreationOfData() {
        val name = "Un Beau nom"
        val description = "et une description"
        val testData: TestData = TestData(name, description)
        require(testData.name == name)
        require(testData.description == description)
    }

    fun testPersistanceOfData() {
        val name = "Un Beau nom"
        val description = "et une description"
        val testData: TestData = TestData(name, description)
        val testDataGotFromElsewhere = TestData(testData.id)
        require(testData.id == testDataGotFromElsewhere.id)
        require(testData.name == testDataGotFromElsewhere.name)
        require(testData.description == testDataGotFromElsewhere.description)
    }

    fun testMutabilityOfData() {
        val name = "Un Beau nom"
        val description = "et une description"
        val testData: TestData = TestData(name, description)
        require(testData.name == name)
        require(testData.description == description)
        val newDescription = "une nouvelle description"
        testData.description = newDescription
        require(testData.description == newDescription)
    }

    fun testAllTypesOfData() {
        val someLong: Long = 67
        val someDouble: Double = 56.9
        val someString: String = "hello tristan"
        val someByteArray: ByteArray = "some data".encodeToByteArray()
        val someInt: Int = 9
        val someShort: Short = 856
        val someFloat: Float = 56.3f
        val someBoolean: Boolean = true
        val testData = DataWithAllTypes(
            someLong,
            someDouble,
            someString,
            someByteArray,
            someInt,
            someShort,
            someFloat,
            someBoolean
        )
        require(testData.someLong == someLong)
        require(testData.someDouble == someDouble)
        require(testData.someString == someString)
        require(testData.someByteArray.contentEquals(someByteArray))
        require(testData.someInt == someInt)
        require(testData.someShort == someShort)
        require(testData.someFloat == someFloat)
        require(testData.someBoolean == someBoolean)
    }

    fun testObservabilityOfData() {
        val name = "Un Beau nom"
        val description = "et une description"
        val testData = TestData(name, description)
        var currentDescription = ""
        val observingJob = testData.asFlow()
            .onEach { currentDescription = it.description }
            .launchIn(CoroutineScope(Dispatchers.IO))
        runBlocking {
            try {
                delay(OBSERVATION_DELAY)
                require(currentDescription == description)
                val newDescription = "une autre description"
                testData.description = newDescription
                delay(OBSERVATION_DELAY)
                require(currentDescription == newDescription)
            } finally {
                observingJob.cancel()
            }
        }
    }

    fun testObservabilityOfProperties() {
        val name = "Un Beau nom"
        val description = "et une description"
        val testData = TestData(name, description)
        var currentDescription = ""
        val observingJob = testData.descriptionAsFlow()
            .onEach { currentDescription = it }
            .launchIn(CoroutineScope(Dispatchers.IO))
        runBlocking {
            try {
                delay(OBSERVATION_DELAY)
                require(currentDescription == description)
                val newDescription = "une autre description"
                testData.description = newDescription
                delay(OBSERVATION_DELAY)
                require(currentDescription == newDescription)
            } finally {
                observingJob.cancel()
            }
        }
    }

    fun testRelationBetweenPersistedData() {
        val name = "Un Beau nom"
        val description = "et une description"
        val testData = TestData(name, description)
        val dataWithRelation: DataWithRelation = DataWithRelation(testData)
        require(dataWithRelation.testData.name == name)
    }

    fun testRelationObservability() {
        val testData = TestData("Un Beau nom", "et une description")
        val dataWithRelation = DataWithChangingRelation(testData)
        var currentRelation: TestData? = null
        val observingJob = dataWithRelation.testDataAsFlow()
            .onEach { currentRelation = it }
            .launchIn(CoroutineScope(Dispatchers.IO))
        runBlocking {
            try {
                delay(OBSERVATION_DELAY)
                require(currentRelation?.id == testData.id)
                val newTestData = TestData("new name", "une autre description")
                dataWithRelation.testData = newTestData
                delay(OBSERVATION_DELAY)
                require(currentRelation?.id == newTestData.id)
            } finally {
                observingJob.cancel()
            }
        }
    }

    fun testObservabilityRedundancy() {
        val testData = VariableTestData("Un Beau nom", "et une description")
        var nameEmissionsCount = 0
        var descriptionEmissionsCount = 0
        val nameObservingJob = testData.nameAsFlow()
            .onEach { nameEmissionsCount++ }
            .launchIn(CoroutineScope(Dispatchers.IO))
        val descriptionObservingJob = testData.descriptionAsFlow()
            .onEach { descriptionEmissionsCount++ }
            .launchIn(CoroutineScope(Dispatchers.IO))
        runBlocking {
            try {
                delay(OBSERVATION_DELAY)
                require(nameEmissionsCount == 1)
                require(descriptionEmissionsCount == 1)
                testData.description = "une autre description"
                delay(OBSERVATION_DELAY)
                require(nameEmissionsCount == 1)
                require(descriptionEmissionsCount == 2)
            } finally {
                nameObservingJob.cancel()
                descriptionObservingJob.cancel()
            }
        }
    }

    fun testDataWithListOfPrimitives() {
        val name = "Un Beau nom"
        val descriptions = listOf("et une description", "et une autre")
        val testData = TestDataWithPrimitiveStaticList(name, descriptions)
        require(testData.descriptions == descriptions)
    }

    fun testDataWithListOfReferences() {
        val name = "Un Beau nom"
        val descriptions = listOf(
            TestData("one name", "one desc"),
            TestData("two name", "two desc")
        )
        val testData = TestDataWithRefStaticList(name, descriptions)
        require(testData.descriptions.map { it.id } == descriptions.map { it.id })
    }

    fun testObservabilityOfListOfReferences() {
        val name = "Un Beau nom"
        val descriptions = listOf(
            TestData("one name", "one desc"),
            TestData("two name", "two desc")
        )
        val otherDescriptions = listOf(
            TestData("three name", "three desc"),
            TestData("four name", "four desc")
        )
        val testData = TestDataWithRefList(name, descriptions)

        var currentObservationFromProperty = emptyList<TestData>()
        var currentObservationFromObject = emptyList<TestData>()
        val propertyObservingJob = testData.descriptionsAsFlow()
            .onEach { currentObservationFromProperty = it }
            .launchIn(CoroutineScope(Dispatchers.IO))
        val objectObservingJob = testData.asFlow()
            .onEach { currentObservationFromObject = it.descriptions }
            .launchIn(CoroutineScope(Dispatchers.IO))
        runBlocking {
            try {
                delay(OBSERVATION_DELAY)
                require(currentObservationFromProperty.map { it.id } == descriptions.map { it.id })
                require(currentObservationFromObject.map { it.id } == descriptions.map { it.id })
                testData.descriptions = otherDescriptions
                delay(OBSERVATION_DELAY)
                require(currentObservationFromProperty.map { it.id } == otherDescriptions.map { it.id })
                require(currentObservationFromObject.map { it.id } == otherDescriptions.map { it.id })
            } finally {
                propertyObservingJob.cancel()
                objectObservingJob.cancel()
            }
        }
    }

    fun testObservabilityOfListOfPrimitives() {
        val name = "Un Beau nom"
        val descriptions = listOf(
            "one desc",
            "two desc"
        )
        val otherDescriptions = listOf(
            "three desc",
            "four desc"
        )
        val testData = TestDataWithPrimitiveList(name, descriptions)

        var currentObservationFromProperty = emptyList<String>()
        var currentObservationFromObject = emptyList<String>()
        val propertyObservingJob = testData.descriptionsAsFlow()
            .onEach { currentObservationFromProperty = it }
            .launchIn(CoroutineScope(Dispatchers.IO))
        val objectObservingJob = testData.asFlow()
            .onEach { currentObservationFromObject = it.descriptions }
            .launchIn(CoroutineScope(Dispatchers.IO))
        runBlocking {
            try {
                delay(OBSERVATION_DELAY)
                require(currentObservationFromProperty == descriptions)
                require(currentObservationFromObject == descriptions)
                testData.descriptions = otherDescriptions
                delay(OBSERVATION_DELAY)
                require(currentObservationFromProperty == otherDescriptions)
                require(currentObservationFromObject == otherDescriptions)
            } finally {
                propertyObservingJob.cancel()
                objectObservingJob.cancel()
            }
        }
    }
}