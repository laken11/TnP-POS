package com.sysbeams.thumbandpin.transaction.bank

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import com.sysbeams.thumbandpin.R

class TransactionAmountInputActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_bank_amount)

        val nextBtn: Button = findViewById(R.id.proceed_btn)
        nextBtn.setOnClickListener {
            startActivity(Intent(this, TransactionBankChoiceListActivity::class.java))
        }
    }
}