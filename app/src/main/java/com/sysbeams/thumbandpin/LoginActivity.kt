package com.sysbeams.thumbandpin

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.sysbeams.thumbandpin.api.httpClient.RetrofitClient
import com.sysbeams.thumbandpin.api.models.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        val loginBtn = findViewById<Button>(R.id.login_btn)
        loginBtn.setOnClickListener {
            authenticate()
        }
    }

    private fun authenticate(){
        val accessCode = findViewById<EditText?>(R.id.access_code).text.toString()
        if (accessCode.isBlank() || accessCode.isEmpty()) {
            Toast.makeText(this@LoginActivity, "Please enter an access code", Toast.LENGTH_LONG)
                .show()
        } else {
            fetchUser(accessCode)
        }
    }

    private fun fetchUser(code: String) {
        val apiService = RetrofitClient.apiService
        val call = apiService.getUser(code)

        call.enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    val user = response.body()
                    user?.let {
                        SharedPreferencesHelper.storeObject(
                            this@LoginActivity,
                            "accessCode",
                            it.code
                        )
                    }
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                } else {
                    Toast.makeText(this@LoginActivity, "Unable to get response", Toast.LENGTH_LONG)
                        .show()
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Toast.makeText(this@LoginActivity, "Invalid merchant code", Toast.LENGTH_LONG)
                    .show()
            }
        })
    }
}