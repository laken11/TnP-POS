package com.sysbeams.thumbandpin

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.ComponentActivity
import com.sysbeams.thumbandpin.enrollment.EnrollmentChoiceListActivity
import com.sysbeams.thumbandpin.transaction.TransactionChoiceListActivity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val enrollmentBtn: ImageView = findViewById(R.id.enrollment_btn)
        val transactionBtn: ImageView = findViewById(R.id.transaction_btn)
        enrollmentBtn.setOnClickListener{
            startActivity(Intent(this, EnrollmentChoiceListActivity::class.java))
        }
        transactionBtn.setOnClickListener{
            startActivity(Intent(this, TransactionChoiceListActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        val enrollmentBtn: ImageView = findViewById(R.id.enrollment_btn)
        enrollmentBtn.setOnClickListener{
            startActivity(Intent(this, EnrollmentChoiceListActivity::class.java))
        }
    }
}