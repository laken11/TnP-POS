package com.sysbeams.thumbandpin.transaction

import android.content.Intent
import android.os.Bundle
import android.widget.RelativeLayout
import androidx.activity.ComponentActivity
import com.sysbeams.thumbandpin.R

class TransactionChoiceListActivity: ComponentActivity()  {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_choice_list)

        val bankTransaction: RelativeLayout = findViewById(R.id.bank_transaction)
        val cardTransaction: RelativeLayout = findViewById(R.id.card_transaction)
        val walletTransaction: RelativeLayout = findViewById(R.id.wallet_transaction)

        bankTransaction.setOnClickListener {
            startActivity(Intent(this@TransactionChoiceListActivity, FingerprintAuthenticationActivity::class.java))
        }

        cardTransaction.setOnClickListener {
            startActivity(Intent(this@TransactionChoiceListActivity, FingerprintAuthenticationActivity::class.java))
        }

        walletTransaction.setOnClickListener {
            startActivity(Intent(this@TransactionChoiceListActivity, FingerprintAuthenticationActivity::class.java))
        }


    }
}