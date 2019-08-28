package com.example.ourapp

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ourapp.db.DbConfig
import com.example.ourapp.db.PlanDb
import com.example.ourapp.modules.PlanDetail
import com.example.ourapp.modules.PlanEdit
import org.litepal.LitePal
import org.w3c.dom.Text
import kotlin.math.sign

class PlanItemAdapter(private var context: Context, private var itemList: MutableList<PlanItem>
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    // 普通的item， footer
    private val TYPE_ITEM: Int = 0
    private val TYPE_FOOTER: Int = 1
    // loading处于第几个状态
    var loadingStatus: Int = 0
    // 上拉加载更多
    private val PULLUPLOADMORE: Int = 0
    // 加载中
    private val LOADINGMORE: Int = 1
    // 没有更多数据了
    private val NOLOADDATA: Int = 2
    inner class PlanItemHolder(view: View) : RecyclerView.ViewHolder(view) {
        var planImage: ImageView = view.findViewById(R.id.plan_image)
        var planDesc: TextView = view.findViewById(R.id.plan_desc)
        var planEdit: TextView = view.findViewById(R.id.plan_eidt)
        var planDel: TextView = view.findViewById(R.id.plan_del)
    }
    inner class PlanFooterHolder(view: View) : RecyclerView.ViewHolder(view) {
        var planLoadingBar: ProgressBar = view.findViewById(R.id.progress_bar)
        var planLoadingText: TextView = view.findViewById(R.id.loading_text)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == TYPE_ITEM) ifreturn@ {
            val view: View = LayoutInflater.from(context).inflate(R.layout.plan_item, parent, false)
            val planItemHolder: PlanItemHolder = PlanItemHolder(view)
            return  planItemHolder
        } else {
            val viewFooter: View = LayoutInflater.from(context).inflate(R.layout.footer_loading, parent, false)
            val planFooterHolder: PlanFooterHolder = PlanFooterHolder(viewFooter)
            return planFooterHolder
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is PlanItemHolder) {
            val planItemHolder: PlanItemHolder = holder as PlanItemHolder
            val id = itemList[position].id
            planItemHolder.planImage.setImageBitmap(itemList[position].imageUri)
            planItemHolder.planDesc.text = itemList[position].desc
            // 点击头像和旅行日记简介的监听方法
            planItemHolder.planImage.setOnClickListener { view: View? -> run {
                this.toPlanDetail(id)
            } }
            planItemHolder.planDesc.setOnClickListener { view: View? -> run {
                this.toPlanDetail(id)
            } }
            // 点击编辑按钮的监听方法
            planItemHolder.planEdit.setOnClickListener { view: View? -> run {
                val detailIntent: Intent = Intent(context, PlanEdit::class.java)
                detailIntent.putExtra("planId", id)
                context.startActivity(detailIntent)
            } }
            // 点击删除按钮的监听方法
            planItemHolder.planDel.setOnClickListener { view: View? -> run {
                val dialog: AlertDialog.Builder = AlertDialog.Builder(context)
                dialog.setTitle("警告")
                    .setMessage("确定要删除旅行日记吗？")
                    .setCancelable(false)
                    .setPositiveButton("删除", DialogInterface.OnClickListener{dialog, which -> run {
                        LitePal.delete(PlanDb::class.java, id)
                        itemList.removeAt(position)
                        notifyDataSetChanged()
                        holder.itemView.scrollTo(0, 0)
                    }})
                    .setNegativeButton("取消", DialogInterface.OnClickListener{dialog, which -> run {
                        // 点击取消按钮的回调函数
                    }})
                    .show()
            } }
        } else if (holder is PlanFooterHolder) {
            val planFooterHolder: PlanFooterHolder = holder as PlanFooterHolder
            checkLoading(planFooterHolder.planLoadingBar, planFooterHolder.planLoadingText)
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (position + 1 == itemCount) {
            return TYPE_FOOTER
        } else {
            return TYPE_ITEM
        }
    }

    override fun getItemCount(): Int {
        return itemList.size + 1
    }

    // 跳转到计划详情界面
    fun toPlanDetail(id: Long) {
        val detailIntent: Intent = Intent(context, PlanDetail::class.java)
        detailIntent.putExtra("planId", id)
        context.startActivity(detailIntent)
    }
    // 处理loading的状态
    fun checkLoading(barView: ProgressBar, textView: TextView) {
        when (loadingStatus) {
            PULLUPLOADMORE -> {
                // 上拉加载更多
                barView.visibility = View.GONE
                textView.text = "上拉加载更多"
            }
            LOADINGMORE -> {
                // 加载中
                barView.visibility = View.VISIBLE
                textView.visibility = View.GONE
                textView.text = "加载中..."
            }
            NOLOADDATA -> {
                // 没有更多数据了
                barView.visibility = View.GONE
                textView.text = "没有更多数据了"
            }
            else -> {

            }
        }
    }
    // 下拉刷新
    fun refresh() {
        Log.d("refresh", "refresh")
    }
}