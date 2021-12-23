package com.tritus.test.model

import com.tritus.test.annotation.Persist
import com.tritus.test.annotation.PersistentId

@Persist
interface TestDataWithRefStaticList {
    @PersistentId
    val id: Long
    var name: String?
    val descriptions: List<TestData>
}
