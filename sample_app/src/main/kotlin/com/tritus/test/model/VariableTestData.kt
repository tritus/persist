package com.tritus.test.model

import com.tritus.test.annotation.Persist
import com.tritus.test.annotation.PersistentId

@Persist
interface VariableTestData {
    @PersistentId
    val id: Long
    var name: String?
    var description: String
}

