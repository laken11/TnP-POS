package com.sysbeams.thumbandpin.transaction.bank

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import com.sysbeams.thumbandpin.R

class TransactionBankChoiceListActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.transaction_bank_choice_list)

        val nextBtn: Button = findViewById(R.id.proceed_btn)
        nextBtn.setOnClickListener {
            startActivity(Intent(this, TransactionBankDetailsActivity::class.java))
        }
    }
}