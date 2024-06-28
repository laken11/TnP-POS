package com.sysbeams.thumbandpin.enrollment.nin

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.sysbeams.thumbandpin.R
import com.sysbeams.thumbandpin.SharedPreferencesHelper
import com.sysbeams.thumbandpin.api.httpClient.RetrofitClient
import com.sysbeams.thumbandpin.api.models.BvnEnrollmentRequest
import com.sysbeams.thumbandpin.api.models.NinEnrollmentRequest
import com.sysbeams.thumbandpin.api.models.UserEnrollment
import com.sysbeams.thumbandpin.enrollment.EnrollmentChoiceListActivity
import com.sysbeams.thumbandpin.enrollment.FingerprintEnrollmentActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NinEnrollmentActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nin_enrollment)
        val backBtn: ImageButton = findViewById(R.id.back)
        backBtn.setOnClickListener {
            finish()
        }
        val processBtn: Button = findViewById(R.id.proceed_btn)
        processBtn.setOnClickListener {
            val ninText = findViewById<EditText?>(R.id.enter_nin).text.toString()
            if(ninText.isBlank() || ninText.isEmpty()){
                Toast.makeText(this@NinEnrollmentActivity, "Please Enter a valid NIN", Toast.LENGTH_LONG).show()
            }
            else{
                val intent = Intent(this@NinEnrollmentActivity, FingerprintEnrollmentActivity::class.java)
                intent.putExtra("id", ninText).putExtra("activity", "ninActivity")
                startActivity(intent)
            }
        }
    }
}