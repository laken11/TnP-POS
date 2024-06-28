package com.sysbeams.thumbandpin.enrollment

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.sysbeams.thumbandpin.MainActivity
import com.sysbeams.thumbandpin.R
import com.sysbeams.thumbandpin.SharedPreferencesHelper
import com.sysbeams.thumbandpin.api.httpClient.RetrofitClient
import com.sysbeams.thumbandpin.api.models.BvnEnrollmentRequest
import com.sysbeams.thumbandpin.api.models.NinEnrollmentRequest
import com.sysbeams.thumbandpin.api.models.User
import com.sysbeams.thumbandpin.api.models.UserEnrollment
import com.sysbeams.thumbandpin.api.models.UserEnrollmentDto
import com.sysbeams.thumbandpin.enrollment.nin.NinEnrollmentSuccessfulActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class EnrollmentInfoActivity: ComponentActivity() {
    @OptIn(ExperimentalEncodingApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bvn_enrollment_info)
        val bvnOrNin = intent.getStringExtra("id").toString()
        val activity = intent.getStringExtra("activity").toString()
        val backBtn: ImageButton = findViewById(R.id.back)
        backBtn.setOnClickListener {
            finish()
        }
        val data = SharedPreferencesHelper.retrieveObject<UserEnrollmentDto>(this@EnrollmentInfoActivity, bvnOrNin)
        if(activity == "ninActivity"){
            getPreEnrollmentDetails(NinEnrollmentRequest(data!!.mBVNorNIN, data.size, Base64.encode(data.template)), null, bvnOrNin, activity)
        }
        if (activity == "bvnActivity"){
            getPreEnrollmentDetails(null, BvnEnrollmentRequest(data!!.mBVNorNIN, data.size, Base64.encode(data.template)), bvnOrNin, activity)
        }
        val proceedBtn: Button = findViewById(R.id.proceed_btn)
        proceedBtn.setOnClickListener {
            if(activity == "ninActivity"){
                val intent = Intent(this, NinEnrollmentSuccessfulActivity::class.java)
                intent.putExtra("id", bvnOrNin)
                startActivity(intent)
            }
            if(activity == "bvnActivity"){
                val intent = Intent(this, AccountEnrollmentInfoActivity::class.java)
                intent.putExtra("id", bvnOrNin)
                startActivity(intent)
            }

        }
    }

    private fun getPreEnrollmentDetails(requestNin: NinEnrollmentRequest?, requestBvn: BvnEnrollmentRequest?, bvnOrNin: String, activity: String){
        if(activity == "ninActivity"){
            val apiService = RetrofitClient.apiService
            val call = apiService.getNinPreEnrollment(requestNin!!)

            call.enqueue(object : Callback<UserEnrollment> {
                override fun onResponse(call: Call<UserEnrollment>, response: Response<UserEnrollment>) {
                    if (response.isSuccessful) {
                        val user = response.body()
                        SharedPreferencesHelper.storeObject(this@EnrollmentInfoActivity, bvnOrNin, user)
                        if (user != null) {
                            setDetails(user)
                        }
                    } else {
                        Toast.makeText(this@EnrollmentInfoActivity, "Unable to get response", Toast.LENGTH_LONG)
                            .show()
                    }
                }

                override fun onFailure(call: Call<UserEnrollment>, t: Throwable) {
                    Toast.makeText(this@EnrollmentInfoActivity, "Unable to get fetch details", Toast.LENGTH_LONG)
                        .show()
                }
            })
        }
        else{
            val apiService = RetrofitClient.apiService
            val call = apiService.getBvnPreEnrollment(requestBvn!!)

            call.enqueue(object : Callback<UserEnrollment> {
                override fun onResponse(call: Call<UserEnrollment>, response: Response<UserEnrollment>) {
                    if (response.isSuccessful) {
                        val user = response.body()
                        SharedPreferencesHelper.storeObject(this@EnrollmentInfoActivity, bvnOrNin, user)
                        if (user != null) {
                            setDetails(user)
                        }
                    } else {
                        Toast.makeText(this@EnrollmentInfoActivity, "Unable to get response", Toast.LENGTH_LONG)
                            .show()
                    }
                }

                override fun onFailure(call: Call<UserEnrollment>, t: Throwable) {
                    Toast.makeText(this@EnrollmentInfoActivity, "Unable to get fetch details", Toast.LENGTH_LONG)
                        .show()
                }
            })
        }


    }

    private fun setDetails(userEnrollment: UserEnrollment){

        findViewById<TextView>(R.id.full_name_text).text =
            String.format("%s %s", userEnrollment.last_name, userEnrollment.first_name)
        findViewById<TextView>(R.id.email_text).text = userEnrollment.email
        findViewById<TextView>(R.id.date_of_birth_text).text = userEnrollment.dob
        findViewById<TextView>(R.id.phone_number_text).text = userEnrollment.phone_number
        findViewById<TextView>(R.id.address_text).text = userEnrollment.address

    }
}