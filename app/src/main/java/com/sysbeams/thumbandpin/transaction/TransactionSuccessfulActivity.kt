package com.sysbeams.thumbandpin.transaction

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import com.sysbeams.thumbandpin.R

class TransactionSuccessfulActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_scucessful)

        val nextBtn: Button = findViewById(R.id.proceed_btn)
        nextBtn.setOnClickListener {
            startActivity(Intent(this, TransactionReceiptActivity::class.java))
        }
    }
}