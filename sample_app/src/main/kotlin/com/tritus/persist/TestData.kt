package com.tritus.persist

import com.tritus.persist.annotation.Persist
import com.tritus.persist.annotation.PersistentId

@Persist
interface TestData {
  @PersistentId
  val id: Long
  val name: String
  val description: String
}