package com.sysbeams.thumbandpin.enrollment.card

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.sysbeams.thumbandpin.R
import com.sysbeams.thumbandpin.SharedPreferencesHelper
import com.sysbeams.thumbandpin.api.httpClient.RetrofitClient
import com.sysbeams.thumbandpin.api.models.BvnEnrollmentRequest
import com.sysbeams.thumbandpin.api.models.CardEnrollmentRequest
import com.sysbeams.thumbandpin.api.models.NinEnrollmentRequest
import com.sysbeams.thumbandpin.api.models.UserEnrollment
import com.sysbeams.thumbandpin.api.models.UserEnrollmentDto
import com.sysbeams.thumbandpin.enrollment.EnrollmentSuccessfulActivity
import com.sysbeams.thumbandpin.enrollment.nin.NinEnrollmentSuccessfulActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class CardEnrollmentInfoActivity: ComponentActivity() {
    private var cardNumber: String = ""
    private var cardHolderName: String = ""
    private var expiry: String = ""

    @OptIn(ExperimentalEncodingApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_enrollment_info)
        cardNumber = intent.getStringExtra("cardNum").toString()
        cardHolderName = intent.getStringExtra("cardHolderName").toString()
        expiry = intent.getStringExtra("expiry").toString()
        val data = SharedPreferencesHelper.retrieveObject<UserEnrollmentDto>(this, intent.getStringExtra("id").toString())
        getPreEnrollmentDetails(CardEnrollmentRequest(Base64.encode(data!!.template), data.size, data.mBVNorNIN, cardNumber, cardHolderName, expiry, "123"), data.mBVNorNIN)
        val proceedBtn: Button = findViewById(R.id.proceed_btn)

        proceedBtn.setOnClickListener {
            val intent = Intent(this, EnrollmentSuccessfulActivity::class.java)
            intent.putExtra("id", data.mBVNorNIN)
            intent.putExtra("activity", "cardEnrollment")
            startActivity(intent)
        }

    }

    private fun getPreEnrollmentDetails(request: CardEnrollmentRequest, bvnOrNin: String){
        val apiService = RetrofitClient.apiService
        val call = apiService.getCardPreEnrollment(request)

        call.enqueue(object : Callback<UserEnrollment> {
            override fun onResponse(call: Call<UserEnrollment>, response: Response<UserEnrollment>) {
                if (response.isSuccessful) {
                    val user = response.body()
                    SharedPreferencesHelper.storeObject(this@CardEnrollmentInfoActivity, bvnOrNin, user)
                    if (user != null) {
                        setDetails()
                    }
                } else {
                    Toast.makeText(this@CardEnrollmentInfoActivity, "Unable to get response", Toast.LENGTH_LONG)
                        .show()
                }
            }

            override fun onFailure(call: Call<UserEnrollment>, t: Throwable) {
                Toast.makeText(this@CardEnrollmentInfoActivity, "Unable to get fetch details", Toast.LENGTH_LONG)
                    .show()
            }
        })
    }

    private fun setDetails(){
        val cardNameText: EditText = findViewById(R.id.card_name_text)
        val cardNumberText: EditText = findViewById(R.id.card_number_text)
        val expiryText: EditText = findViewById(R.id.expiry_text)
        cardNameText.setText(cardHolderName)
        cardNumberText.setText(cardNumber)
        expiryText.setText(expiry)
    }

}