package com.sysbeams.thumbandpin.api.models


data class Enrollment(
    val type: String,
    val first_name: String,
    val last_name: String,
    val phone_number: String,
    val address: String,
    val DOB: String,
    val id: String,
    val Ref: String
)

data class EnrollmentRequest(
    val type: String,
    val bvn: String?,
    val nin: String?,
    val merchant_code: String,
    val account_ids: Array<String>?
)
