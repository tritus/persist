package com.tritus.test

import com.tritus.test.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

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
}