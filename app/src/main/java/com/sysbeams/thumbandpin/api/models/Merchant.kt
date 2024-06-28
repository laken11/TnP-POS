package com.sysbeams.thumbandpin.api.models

data class Merchant(
    val id: String,
    val name: String,
    val code: String,
    val email: String,
    val phoneNumber: String,
    val accountBalance: Int,
    val cacNumber: String
)