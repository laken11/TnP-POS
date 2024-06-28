package com.sysbeams.thumbandpin.api.models

data class Account(
    val number: String,
    val type: String,
    val account_balance: Int,
    val bank: String
)