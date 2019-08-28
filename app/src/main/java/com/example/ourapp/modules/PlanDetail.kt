package com.example.ourapp.modules

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.example.ourapp.R
import com.example.ourapp.common.Utils
import com.example.ourapp.db.PlanDb
import org.litepal.LitePal

class PlanDetail : AppCompatActivity() {
    private lateinit var plan: PlanDb
    private lateinit var planContent: TextView
    private lateinit var planDate: TextView
    private lateinit var planWrap: LinearLayout
    private var UPDATEDPLAN: Int = 0
    private var planId: Long = 0
    // 处理message
    private var handler: Handler = Handler {
        when(it.what) {
            UPDATEDPLAN -> {
                planDate.text = plan.date
                planContent.text = plan.content
                addImageToPlanWrap()
            }
            else -> {
            }
        }
        false
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.module_plan_detail)
        // 获取日期 textView
        planDate = findViewById(R.id.date) as TextView
        planContent = findViewById(R.id.plan_content) as TextView
        planWrap = findViewById(R.id.plan_wrap) as LinearLayout
        val intent: Intent = intent
        planId = intent.getLongExtra("planId", 0)
        getPlanDetail()
    }
    private fun getPlanDetail () {
        plan =  LitePal.find(PlanDb::class.java, planId)
        Thread(Runnable {
            try {
                val message = Message()
                message.what = UPDATEDPLAN
                handler.sendMessage(message)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }).start()
    }
    private fun addImageToPlanWrap() {
        val images = plan.imgs.split(",")
        for (image in images) {
            val imageView: ImageView = ImageView(this)
            val layoutParams: LinearLayout.LayoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
            layoutParams.topMargin = 5
            imageView.layoutParams = layoutParams
            imageView.scaleType = ImageView.ScaleType.FIT_XY
            imageView.setImageBitmap(Utils.base64ToBitmap(image))
            planWrap.addView(imageView)
        }
    }
}
