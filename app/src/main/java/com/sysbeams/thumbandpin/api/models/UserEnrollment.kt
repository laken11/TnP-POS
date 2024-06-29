package com.sysbeams.thumbandpin.api.models


data class UserEnrollment(
    val first_name: String,
    val last_name: String,
    val phone_number: String,
    val address: String,
    val email: String,
    val dob: String,
    val bvn: String?,
    val nin: String?,
    val accounts: List<Account>
)

data class BvnEnrollmentRequest(
    val bvn: String,
    val size: Int,
    val template: String
)

data class NinEnrollmentRequest(
    val nin: String,
    val size: Int,
    val template: String
)

data class CardEnrollmentRequest(
    val template: String,
    val size: Int,
    val bvn: String,
    val card_number: String,
    val card_holder: String,
    val expiration_date: String,
    val cvv: String
)