package com.sysbeams.thumbandpin.api.models

data class User(
    val id: String,
    val name: String,
    val code: String,
    val email: String,
    val phoneNumber: String,
    val accountBalance: Int,
    val cacNumber: String,
    val transactions: List<Transaction>
)

data class UserEnrollmentDto(val mBVNorNIN: String, val size: Int, val template: ByteArray)