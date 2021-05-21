package com.tritus

fun main() {
    val test: Test = TestProvider.new("Un Beau nom", "et une description")
    println("name: ${test.name}, description: ${test.description}")
}