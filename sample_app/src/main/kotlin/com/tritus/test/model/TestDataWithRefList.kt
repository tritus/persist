package com.tritus.test.model

import com.tritus.test.annotation.Persist
import com.tritus.test.annotation.PersistentId

@Persist
interface TestDataWithRefList {
    @PersistentId
    val id: Long
    var name: String?
    var descriptions: List<TestData>
}