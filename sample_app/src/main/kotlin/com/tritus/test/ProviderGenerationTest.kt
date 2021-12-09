package com.tritus.test

import kotlin.coroutines.CoroutineContext

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

    fun testObservabilityOfData() {
        //val name = "Un Beau nom"
        //val description = "et une description"
        //val testData: TestData = TestDataProvider.new(name, description)
        //var output = ""
        //val scope = CoroutineScope(Dispatchers.Main)
        //testData.nameAsObservable().onEach { output = it }.launchIn(scope)
        //runBlocking {
        //    wait(300)
        //    require(output == name)
        //    testData.name = "new name"
        //    wait(300)
        //    require(output == "new name")
        //}
    }
}