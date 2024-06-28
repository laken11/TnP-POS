package com.sysbeams.thumbandpin.enrollment

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import androidx.activity.ComponentActivity
import com.sysbeams.thumbandpin.MainActivity
import com.sysbeams.thumbandpin.R
import com.sysbeams.thumbandpin.enrollment.bvn.BvnEnrollmentActivity
import com.sysbeams.thumbandpin.enrollment.card.CardEnrollmentActivity
import com.sysbeams.thumbandpin.enrollment.nin.NinEnrollmentActivity

class EnrollmentChoiceListActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enrollment_choice_list)
        val cardEnrollmentActionBtn: ImageView = findViewById(R.id.card_enrollment_action)
        val bvnEnrollmentActionBtn: ImageView = findViewById(R.id.bvn_enrollment_action)
        val ninEnrollmentActionBtn: ImageView = findViewById(R.id.nin_enrollment_action)
        val backBtn: ImageButton = findViewById(R.id.back)
        backBtn.setOnClickListener {
            startActivity(Intent(this@EnrollmentChoiceListActivity, MainActivity::class.java))
        }
        cardEnrollmentActionBtn.setOnClickListener{
            startActivity(Intent(this, CardEnrollmentActivity::class.java))
        }
        bvnEnrollmentActionBtn.setOnClickListener{
            startActivity(Intent(this, BvnEnrollmentActivity::class.java))
        }
        ninEnrollmentActionBtn.setOnClickListener{
            startActivity(Intent(this, NinEnrollmentActivity::class.java))
        }
    }
}

