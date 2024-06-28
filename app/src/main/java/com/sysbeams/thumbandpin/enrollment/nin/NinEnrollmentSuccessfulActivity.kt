package com.sysbeams.thumbandpin.enrollment.nin

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
import com.sysbeams.thumbandpin.api.models.Account
import com.sysbeams.thumbandpin.api.models.Enrollment
import com.sysbeams.thumbandpin.api.models.EnrollmentRequest
import com.sysbeams.thumbandpin.api.models.UserEnrollment
import com.sysbeams.thumbandpin.enrollment.EnrollmentChoiceListActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NinEnrollmentSuccessfulActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nin_enrollment_successful)
        val backBtn = findViewById<Button>(R.id.back_to_home)
        backBtn.setOnClickListener {
            startActivity(Intent(this@NinEnrollmentSuccessfulActivity, EnrollmentChoiceListActivity::class.java))
        }
        val nin = intent.getStringExtra("id").toString()
        val accessCode = SharedPreferencesHelper.retrieveObject<String>(this@NinEnrollmentSuccessfulActivity, "accessCode").toString()
        val accounts = SharedPreferencesHelper.retrieveObject<UserEnrollment>(this@NinEnrollmentSuccessfulActivity, nin)!!.accounts
        enroll(EnrollmentRequest("NIN", null, nin, accessCode,  null), accounts)
    }

    private fun enroll(request: EnrollmentRequest?, accounts: List<Account>){
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
                                setDetails(user, accounts)
                            }
                        } else {
                            Toast.makeText(this@NinEnrollmentSuccessfulActivity, "Unable to get response", Toast.LENGTH_LONG)
                                .show()
                        }
                    }

                    override fun onFailure(call: Call<Enrollment>, t: Throwable) {
                        Toast.makeText(this@NinEnrollmentSuccessfulActivity, "Unable to get fetch details", Toast.LENGTH_LONG)
                            .show()
                    }
                })
            }
        }
    }

    private fun setDetails(userEnrollment: Enrollment, accounts: List<Account>){
        findViewById<TextView>(R.id.enrollment_number).text = String.format("%s%s", "Enrollment ID: ", userEnrollment.Ref)
        val copyBtn = findViewById<ImageButton>(R.id.copy)
        val copy2Btn = findViewById<ImageButton>(R.id.copy_2)
        copyBtn.setOnClickListener {
            copyToClipboard(this, userEnrollment.Ref)
        }
        copy2Btn.setOnClickListener {
            copyToClipboard(this, accounts[0].number)
        }
        findViewById<TextView>(R.id.account_name_text).text = String.format("%s %s", userEnrollment.last_name, userEnrollment.first_name)
        findViewById<TextView>(R.id.account_number_text).text = accounts[0].number
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