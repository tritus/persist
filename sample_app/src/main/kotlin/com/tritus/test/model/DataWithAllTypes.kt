package com.tritus.test.model

import com.tritus.test.annotation.Persist
import com.tritus.test.annotation.PersistentId

@Persist
interface DataWithAllTypes {
    @PersistentId
    val id: Long
    val someLong: Long
    val someDouble: Double
    val someString: String
    val someByteArray: ByteArray
    val someInt: Int
    val someShort: Short
    val someFloat: Float
    val someBoolean: Boolean
}