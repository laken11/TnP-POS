package com.sysbeams.thumbandpin.enrollment

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.sysbeams.thumbandpin.R
import com.sysbeams.thumbandpin.SharedPreferencesHelper
import com.sysbeams.thumbandpin.api.models.Account
import com.sysbeams.thumbandpin.api.models.UserEnrollment

class AccountEnrollmentInfoActivity: ComponentActivity() {
    private var accountNumberList: MutableList<String> = mutableListOf()
    private var selectionStateMap = HashMap<Int, Boolean>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_accounts_info)
        val bvnOrNin = intent.getStringExtra("id").toString()
        val backBtn: ImageButton = findViewById(R.id.back)
        backBtn.setOnClickListener {
            finish()
        }
        val processBtn: Button = findViewById(R.id.proceed_btn)
        processBtn.setOnClickListener {
            val intent = Intent(this, EnrollmentSuccessfulActivity::class.java)
            intent.putExtra("id", bvnOrNin)
            intent.putExtra("accounts", accountNumberList.toTypedArray())
            startActivity(intent)
        }
        val accounts = SharedPreferencesHelper.retrieveObject<UserEnrollment>(this@AccountEnrollmentInfoActivity, bvnOrNin)!!.accounts
        setAccounts(accounts)
    }

    private fun setAccounts(accounts: List<Account>){
        val parentLayout: LinearLayout = findViewById(R.id.account_details_layout)
        for ((index, account) in accounts.withIndex()) {
            // Create RelativeLayout (frame_10)
            val frame10 = RelativeLayout(this)
            val layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                resources.getDimensionPixelSize(R.dimen.frame_10_height)
            )
            layoutParams.setMargins(
                resources.getDimensionPixelSize(R.dimen.frame_10_margin_start),
                resources.getDimensionPixelSize(R.dimen.frame_10_margin_top),
                resources.getDimensionPixelSize(R.dimen.frame_10_margin_end),
                resources.getDimensionPixelSize(R.dimen.layout_margin_bottom)
            )
            frame10.layoutParams = layoutParams
            frame10.setBackgroundResource(R.drawable.frame_10)
            frame10.elevation = resources.getDimension(R.dimen.elevation)

            // Create ImageView (imageView)
            val imageView = ImageView(this)
            imageView.id = View.generateViewId()
            val imageViewParams = RelativeLayout.LayoutParams(
                resources.getDimensionPixelSize(R.dimen.image_width),
                resources.getDimensionPixelSize(R.dimen.image_height)
            )
            imageViewParams.addRule(RelativeLayout.ALIGN_PARENT_START)
            imageViewParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
            imageViewParams.setMargins(
                resources.getDimensionPixelSize(R.dimen.image_margin_start),
                0,
                0,
                resources.getDimensionPixelSize(R.dimen.image_margin_bottom)
            )
            imageView.layoutParams = imageViewParams
            imageView.setImageResource(R.drawable.bank)

            // Create RelativeLayout (frame_27)
            val frame27 = RelativeLayout(this)
            val frame27Params = RelativeLayout.LayoutParams(
                resources.getDimensionPixelSize(R.dimen.frame27_width),
                resources.getDimensionPixelSize(R.dimen.frame27_height)
            )
            frame27Params.addRule(RelativeLayout.ALIGN_PARENT_END)
            frame27Params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
            frame27Params.addRule(RelativeLayout.END_OF, imageView.id)
            frame27Params.setMargins(
                resources.getDimensionPixelSize(R.dimen.frame27_margin_start),
                0,
                resources.getDimensionPixelSize(R.dimen.frame27_margin_end),
                resources.getDimensionPixelSize(R.dimen.frame27_margin_bottom)
            )
            frame27.layoutParams = frame27Params

            // Create TextViews for frame_27
            val accNumber = TextView(this)
            accNumber.id = View.generateViewId()
            val accNumberParams = RelativeLayout.LayoutParams(
                resources.getDimensionPixelSize(R.dimen.acc_number_width),
                resources.getDimensionPixelSize(R.dimen.acc_number_height)
            )
            accNumberParams.addRule(RelativeLayout.ALIGN_PARENT_TOP)
            accNumber.layoutParams = accNumberParams
            accNumber.gravity = Gravity.CENTER_VERTICAL
            accNumber.text = account.number
            accNumber.setTextAppearance(this, R.style.acc_number)

            val bankName = TextView(this)
            val bankNameParams = RelativeLayout.LayoutParams(
                resources.getDimensionPixelSize(R.dimen.bank_name_width),
                resources.getDimensionPixelSize(R.dimen.bank_name_height)
            )
            bankNameParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
            bankNameParams.setMargins(0, 0, 0, resources.getDimensionPixelSize(R.dimen.bank_name_margin_bottom))
            bankName.layoutParams = bankNameParams
            bankName.gravity = Gravity.CENTER_VERTICAL
            bankName.text = account.bank
            bankName.setTextAppearance(this, R.style.bank_name_text)

            val accountType = TextView(this)
            val accountTypeParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, // Dynamic width
                RelativeLayout.LayoutParams.WRAP_CONTENT
            )
            accountTypeParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
            accountTypeParams.setMargins(0, 0, 0, resources.getDimensionPixelSize(R.dimen.account_type_margin_bottom))
            accountType.layoutParams = accountTypeParams
            accountType.gravity = Gravity.CENTER_VERTICAL
            accountType.text = account.type
            accountType.setTextAppearance(this, R.style.account_type)

            // Create ImageButton (ellipse)
            val ellipseButton = ImageButton(this)
            ellipseButton.id = View.generateViewId()
            val ellipseParams = RelativeLayout.LayoutParams(
                resources.getDimensionPixelSize(R.dimen.ellipse_width),
                resources.getDimensionPixelSize(R.dimen.ellipse_height)
            )
            ellipseParams.addRule(RelativeLayout.ALIGN_PARENT_END)
            ellipseParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
            ellipseParams.setMargins(
                0,
                0,
                resources.getDimensionPixelSize(R.dimen.ellipse_margin_end),
                resources.getDimensionPixelSize(R.dimen.ellipse_margin_bottom)
            )
            ellipseButton.layoutParams = ellipseParams
            ellipseButton.setImageResource(R.drawable.ellipse)
            ellipseButton.visibility = View.VISIBLE // Ensure it is visible

            // Initialize selection state for this button
            selectionStateMap[index] = false
            ellipseButton.setOnClickListener {
                val isSelected = selectionStateMap[index] ?: false
                if (!isSelected) {
                    ellipseButton.setImageResource(R.drawable.tick_circle)
                    val accountNumber = accNumber.text.toString()
                    accountNumberList.add(accountNumber)
                } else {
                    ellipseButton.setImageResource(R.drawable.ellipse)
                    val accountNumber = accNumber.text.toString()
                    accountNumberList.remove(accountNumber)
                }
                // Toggle selection state
                selectionStateMap[index] = !isSelected
            }

            // Add views to frame_27
            frame27.addView(accNumber)
            frame27.addView(bankName)
            frame27.addView(accountType)

            // Add views to frame_10
            frame10.addView(frame27)
            frame10.addView(ellipseButton)
            frame10.addView(imageView)

            // Add frame_10 to parent layout
            parentLayout.addView(frame10)
        }
    }
}