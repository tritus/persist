package com.tritus.test

import com.tritus.test.model.DataWithRelation
import com.tritus.test.model.TestData
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

object ProviderGenerationTest {
    fun testCreationOfData() {
        val name = "Un Beau nom"
        val description = "et une description"
        val testData: TestData = TestDataProvider.new(name, description)
        require(testData.name == name)
        require(testData.description == description)
    }

    fun testPersistanceOfData() {
        val name = "Un Beau nom"
        val description = "et une description"
        val testData: TestData = TestDataProvider.new(name, description)
        val testDataGotFromElsewhere = TestDataProvider.retrieve(testData.id)
        require(testData.id == testDataGotFromElsewhere.id)
        require(testData.name == testDataGotFromElsewhere.name)
        require(testData.description == testDataGotFromElsewhere.description)
    }

    fun testMutabilityOfData() {
        val name = "Un Beau nom"
        val description = "et une description"
        val testData: TestData = TestDataProvider.new(name, description)
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
        val testData = DataWithAllTypesProvider.new(
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
        val testData = TestDataProvider.new(name, description)
        var currentDescription = ""
        val observingJob = testData.asFlow()
            .onEach { currentDescription = it.description }
            .launchIn(CoroutineScope(Dispatchers.IO))
        runBlocking {
            try {
                delay(50)
                require(currentDescription == description)
                val newDescription = "une autre description"
                testData.description = newDescription
                delay(50)
                require(currentDescription == newDescription)
            } finally {
                observingJob.cancel()
            }
        }
    }

    fun testObservabilityOfProperties() {
        val name = "Un Beau nom"
        val description = "et une description"
        val testData = TestDataProvider.new(name, description)
        var currentDescription = ""
        val observingJob = testData.descriptionAsFlow()
            .onEach { currentDescription = it }
            .launchIn(CoroutineScope(Dispatchers.IO))
        runBlocking {
            try {
                delay(50)
                require(currentDescription == description)
                val newDescription = "une autre description"
                testData.description = newDescription
                delay(50)
                require(currentDescription == newDescription)
            } finally {
                observingJob.cancel()
            }
        }
    }

    fun testRelationBetweenPersistedData() {
        val name = "Un Beau nom"
        val description = "et une description"
        val testData = TestDataProvider.new(name, description)
        val dataWithRelation: DataWithRelation = DataWithRelationProvider.new(testData)
        require(dataWithRelation.testData.name == name)
    }
}