package com.tritus.persist

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
}