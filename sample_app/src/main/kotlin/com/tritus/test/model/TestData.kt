package com.tritus.test.model

import com.tritus.test.annotation.Persist
import com.tritus.test.annotation.PersistentId

@Persist
interface TestData {
  @PersistentId
  val id: Long
  val name: String?
  var description: String
}