package com.tritus.persist

import com.tritus.persist.Persist

@Persist
interface TestData {
    val name: String
    val description: String
}