package com.tritus.test

import com.tritus.test.annotation.Persist
import com.tritus.test.annotation.PersistentId

@Persist
interface TestData {
  @PersistentId
  val id: Long
  var name: String?
  val description: String
}

@Persist
interface InvalidTestData {
  @PersistentId
  val id: Long
  var name: String?
  val description: String
}