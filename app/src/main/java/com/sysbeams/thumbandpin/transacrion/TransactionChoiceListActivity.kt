package com.sysbeams.thumbandpin.transacrion

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

        }

        cardTransaction.setOnClickListener {

        }

        walletTransaction.setOnClickListener {

        }


    }
}