package com.sysbeams.thumbandpin.enrollment

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.sysbeams.thumbandpin.R
import com.sysbeams.thumbandpin.SharedPreferencesHelper
import com.sysbeams.thumbandpin.api.httpClient.RetrofitClient
import com.sysbeams.thumbandpin.api.models.Enrollment
import com.sysbeams.thumbandpin.api.models.EnrollmentRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EnrollmentSuccessfulActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bvn_enrollment_successful)
        val backBtn = findViewById<Button>(R.id.back_to_home)
        backBtn.setOnClickListener {
            startActivity(Intent(this@EnrollmentSuccessfulActivity, EnrollmentChoiceListActivity::class.java))
        }
        val bvnOrNin = intent.getStringExtra("id").toString()
        val accounts = intent.getStringArrayExtra("accounts")
        val accessCode = SharedPreferencesHelper.retrieveObject<String>(this, "accessCode").toString()
        val enrollment = EnrollmentRequest("BVN", bvnOrNin, null, accessCode, accounts)
        enroll(enrollment)
    }

    private fun enroll(request: EnrollmentRequest?){
        when (request) {
            null -> {

            }
            else -> {
                val apiService = RetrofitClient.apiService
                val call = apiService.enroll(request)

                call.enqueue(object : Callback<Enrollment> {
                    override fun onResponse(call: Call<Enrollment>, response: Response<Enrollment>) {
                        if (response.isSuccessful) {
                            val user = response.body()
                            if (user != null) {
                                setDetails(user)
                            }
                        } else {
                            Toast.makeText(this@EnrollmentSuccessfulActivity, "Unable to get response", Toast.LENGTH_LONG)
                                .show()
                        }
                    }

                    override fun onFailure(call: Call<Enrollment>, t: Throwable) {
                        Toast.makeText(this@EnrollmentSuccessfulActivity, "Unable to get fetch details", Toast.LENGTH_LONG)
                            .show()
                    }
                })
            }
        }
    }

    private fun setDetails(userEnrollment: Enrollment){
        findViewById<TextView>(R.id.enrollment_number).text = String.format("%s%s", "Enrollment ID: ", userEnrollment.Ref)
        val copyBtn = findViewById<ImageButton>(R.id.copy)
        copyBtn.setOnClickListener {
            copyToClipboard(this, userEnrollment.Ref)
        }
    }

    private fun copyToClipboard(context: Context, text: String) {
        // Get the ClipboardManager
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        // Create a ClipData object with the text
        val clip = ClipData.newPlainText("label", text)

        // Set the ClipData to the ClipboardManager
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "Text copied", Toast.LENGTH_LONG).show()
    }
}