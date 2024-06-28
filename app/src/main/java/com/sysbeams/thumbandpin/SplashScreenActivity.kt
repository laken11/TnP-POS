package com.sysbeams.thumbandpin

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import com.telpo.tps550.api.fingerprint.FingerPrint

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        FingerPrint.fingerPrintPower(1)


        // Add a delay to show the splash screen for a few seconds
        Handler(Looper.getMainLooper()).postDelayed({
            val accessCode = SharedPreferencesHelper.retrieveObject<String>(this, "accessCode")
            if (accessCode.isNullOrBlank()) {
                startActivity(Intent(this, LoginActivity::class.java))
            } else {
                startActivity(Intent(this, MainActivity::class.java))
            }
            finish()
        }, 3000) // 3000 milliseconds delay (3 seconds)
    }
}