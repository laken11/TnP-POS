package com.sysbeams.thumbandpin.api.httpClient


import com.sysbeams.thumbandpin.api.models.BvnEnrollmentRequest
import com.sysbeams.thumbandpin.api.models.Enrollment
import com.sysbeams.thumbandpin.api.models.EnrollmentRequest
import com.sysbeams.thumbandpin.api.models.NinEnrollmentRequest
import com.sysbeams.thumbandpin.api.models.User
import com.sysbeams.thumbandpin.api.models.UserEnrollment
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @GET("/merchants/by-code/{code}")
    fun getUser(@Path("code") code: String): Call<User>

    @POST("/enrollments/bvn")
    fun getBvnPreEnrollment(@Body request: BvnEnrollmentRequest): Call<UserEnrollment>

    @POST("/enrollments/nin")
    fun getNinPreEnrollment(@Body request: NinEnrollmentRequest): Call<UserEnrollment>

    @POST("/enrollments")
    fun enroll(@Body request: EnrollmentRequest): Call<Enrollment>
}