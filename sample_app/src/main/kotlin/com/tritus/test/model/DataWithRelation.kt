package com.tritus.test.model

import com.tritus.test.annotation.Persist
import com.tritus.test.annotation.PersistentId

@Persist
interface DataWithRelation {
    @PersistentId
    val id: Long
    val testData: TestData
}