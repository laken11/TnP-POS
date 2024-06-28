package com.sysbeams.thumbandpin.api.models

data class Transaction(
    val id: String,
    val dateCreated: String,
    val status: String,
    val merchant: Merchant,
    val amount: Int
)