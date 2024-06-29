package com.sysbeams.thumbandpin.transaction

import android.os.Bundle
import androidx.activity.ComponentActivity
import com.sysbeams.thumbandpin.R

class TransactionUnsuccessfulActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_failed)
    }
}