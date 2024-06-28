package com.sysbeams.thumbandpin.enrollment.bvn

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.sysbeams.thumbandpin.R
import com.sysbeams.thumbandpin.enrollment.FingerprintEnrollmentActivity

class BvnEnrollmentActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bvn_enrollment)
        val proceedBtn = findViewById<Button>(R.id.proceed_btn)
        val backBtn: ImageButton = findViewById(R.id.back)
        backBtn.setOnClickListener {
            finish()
        }
        proceedBtn.setOnClickListener {
            val bvnText = findViewById<EditText?>(R.id.bvn).text.toString()
            if(bvnText.isBlank() || bvnText.isEmpty()){
                Toast.makeText(this@BvnEnrollmentActivity, "Please Enter a valid BVN", Toast.LENGTH_LONG).show()
            }
            else{
                val intent = Intent(this@BvnEnrollmentActivity, FingerprintEnrollmentActivity::class.java)
                intent.putExtra("id", bvnText)
                intent.putExtra("activity", "bvnActivity")
                startActivity(intent)
            }
        }
    }
}