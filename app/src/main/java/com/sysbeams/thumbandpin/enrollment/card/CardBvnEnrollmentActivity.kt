package com.sysbeams.thumbandpin.enrollment.card

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.sysbeams.thumbandpin.R
import com.sysbeams.thumbandpin.enrollment.FingerprintEnrollmentActivity

class CardBvnEnrollmentActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bvn_enrollment)
        val cardNum = intent.getStringExtra("cardNum").toString()
        val expiry = intent.getStringExtra("expiry").toString()
        val cardHolderName = intent.getStringExtra("cardHolderName").toString()
        val proceedBtn: Button = findViewById(R.id.proceed_btn)
        val bvnText = findViewById<EditText?>(R.id.bvn).text
        proceedBtn.setOnClickListener {
            if(bvnText.isNullOrEmpty()){
                Toast.makeText(this, "Please enter your bvn number", Toast.LENGTH_LONG).show()
            }else{
                val intent = Intent(this, FingerprintEnrollmentActivity::class.java)
                intent.putExtra("activity", "cardEnrollment")
                intent.putExtra("cardNum", cardNum)
                intent.putExtra("expiry", expiry)
                intent.putExtra("id", bvnText)
                intent.putExtra("cardHolderName", cardHolderName)
                startActivity(intent)
            }
        }

    }
}