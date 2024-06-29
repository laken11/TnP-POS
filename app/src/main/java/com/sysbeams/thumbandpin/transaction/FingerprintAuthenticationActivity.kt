package com.sysbeams.thumbandpin.transaction

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import com.sysbeams.thumbandpin.R
import com.sysbeams.thumbandpin.transaction.bank.TransactionAmountInputActivity

class FingerprintAuthenticationActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_authentication)
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this@FingerprintAuthenticationActivity, TransactionAmountInputActivity::class.java))
        }, 3000)
    }
}