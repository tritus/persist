#Persist
####A library to automatically persist data in kotlin
| :exclamation:  This project is currently a Work in Progress. It is not intended to be used by anyone except myself for now until it is in a state I will consider safe and production ready |
|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
___
##Objective
This library is aimed at developpers who don't want to be bothered by database implementations and boilerplate code. 
It will automatically persist data that you want to be persistent without having you writing more code than just a 
single annotation on the data you want to be persistent.
___
##Setup
You just have to use the gradle plugin in your build.gradle file like so

_build.gradle.kts_
```
plugins {
    ...
    id("com.tritus.persist") version("0.1")
}
...
```
It will add all the required dependancies and apply all the required gradle plugins.

Then, you will have access to the `@Persist` and `@PersistentId` annotations.

Your data format must be an interface 
and the properties should only be of type `Long`, `Double`, `String`, `ByteArray`, `Int`, `Short`, `Float`, `Boolean`
and types annotated with `@Persist`.
Also `List<T>` where `T` is of the previous types is also supported.

You also have to add an identifier property of type `Long` enabling you to get that data later on. 
Annotate this id with `@PersistentId`. 

_ExampleData.kt_
```
@Persist
interface ExampleData {
    @PersistentId
    val id: Long,
    val name: String,
    var descriptions: List<String>
}
```
And Voilà! You're good to go!
___
##Usage
###Initialize and retrieve data
You can initialize your data the way you would do it normally without persistence. You can not set the id.
```
val exampleData = ExampleData("my data", listOf("first desc", "second desc"))
```
Don't forget to store the id of your data if you want to retrieve it later on.
```
val exampleDataId = exampleData.id
someDumbKeyValueStorage.store("myDataId", exampleDataId)
```
And when you want to retrieve it (like after an app restart for instance) you can initialize your data with the id to 
get it back.
```
val previousDataId = someDumbKeyValueStorage.get("myDataId")
val previousData = ExampleData(previousDataId)
```
###Observe data
Given a `@Persist` annotated data interface, you can observe it and its properties using kotlin `Flow`
```
val someData = ExampleData(someId)
someData.asFlow() // Any changes to the properties will result in an emission here.
someData.descriptionsAsFlow() // Any changes to the descriptions property will result in an emission here.
```
Only mutable properties has `myPropertyAsFlow()` accessors since immutable properties cannot change over time.
___
##Example
_build.gradle.kts_
```
plugins {
    ...
    id("com.tritus.persist") version("0.1")
}
...
```
_Models.kt_
```
@Persist
interface Person {
    @PersistentId
    val id: Long,
    val name: String,
    var descriptions: List<String>,
    var clothes: List<Cloth>
}

@Persist
interface Cloth {
    @PersistentId
    val id: Long,
    val name: String,
    val sizeMeters: Float
}
```
_Action.kt_
```
fun someMethodInApp() {
    val newPerson = Person(
        name = "Tristan",
        descritptions = listOf("nice person", "developer", "likes persistence"),
        clothes = listOf(
            Cloth("pants", "1.20"),
            Cloth("shoes", "0.33")
    )
    
    val personId = newPerson.id
    
    ... do stuff ...
    
    val previouslyCreatedPerson = Person(personId)
}   
```
Example of usages can also be found in the `sample_app` module of this project.
___
##Limitations
- Currently, this library does not support migration of saved data. This is my major priority.
- It should work on any project using kotlin (android, multiplatform, native, etc.) but it hasn't been tested yet
- After annotating your data to make it persistent, you may want to build your app to have access to the constructors for your data. An IntelliJ plugin is intended to be developped to prevent this issue but it is quite low in my priorities.
- Currently, the plugin and the lib are not published and is only accessible through this project. When I'll be happy with the state of this lib, I'll publish it on regular repositories. 
- A lot of generated files are added to your project while building in a sqldelight folder. I may try to restrain this behavior later on. Same, not a priority.
___
##Credits
This library uses different other beautiful libraries under the hood:

- [cashapp/sqldelight](https://github.com/cashapp/sqldelight/)
- [google/ksp](https://github.com/google/ksp/)
- [square/kotlinpoet](https://github.com/square/kotlinpoet/)
