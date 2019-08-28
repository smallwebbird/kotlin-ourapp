package com.example.ourapp

import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import android.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.ourapp.common.Utils
import com.example.ourapp.db.DbConfig
import com.example.ourapp.db.PlanDb
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.litepal.LitePal
import java.io.IOException

class MainActivity : AppCompatActivity(), PlanItemTouchHelperAdapter {
    private var ENDRFRESH: Int = 0  // 刷新数据
    private var NODATA: Int = 1 // 已经是最新的数据了
    private lateinit var floatActionButton: FloatingActionButton
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private var itemList: MutableList<PlanItem> = mutableListOf()
    private lateinit var planItemAdapter: PlanItemAdapter
    // 处理message
    private var handler: Handler = Handler {
        when(it.what) {
            ENDRFRESH -> {
                swipeRefreshLayout.isRefreshing = false
                planItemAdapter.notifyDataSetChanged()
            }
            NODATA -> {
                swipeRefreshLayout.isRefreshing = false
                Toast.makeText(this, "已经是最新数据了", Toast.LENGTH_SHORT).show()
            }
            else -> {

            }
        }
        false
    }
    private var total: Int = 0
    private var pageSize: Int = 5
    private var page: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 悬浮按钮
        floatActionButton = findViewById(R.id.floatActionButton) as FloatingActionButton
        floatActionButton.setOnClickListener { view: View? -> run {
            val addIntent: Intent = Intent(this, com.example.ourapp.modules.PlanAdd::class.java)
            startActivity(addIntent)
        } }

        // 获取下拉刷新组件
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout) as SwipeRefreshLayout

        // 获取recycle_view
        val recyclerView: RecyclerView = findViewById(R.id.recycler_view) as RecyclerView
        val linearLayoutManager: LinearLayoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = linearLayoutManager

        // 初始化适配器类
        planItemAdapter = PlanItemAdapter(this, itemList)
        recyclerView.adapter = planItemAdapter

        // itemTouchHelper类绑定到recyclerView
        val callback: PlanItemTouchHelperCallback = PlanItemTouchHelperCallback(this)
        val touchHelper: PlanItemTouchHelper = PlanItemTouchHelper(callback)
        touchHelper.attachToRecyclerView(recyclerView)

        // 实现监听
        recyclerView.addOnScrollListener(RecyclerListener())
        // 下拉刷新监听
        swipeRefreshLayout.setColorSchemeColors(resources.getColor(R.color.colorPrimary))
        swipeRefreshLayout.setOnRefreshListener{
            Thread(Runnable {
                try {
                    getDataCount()
                    var message: Message = Message()
                    planItemAdapter.refresh()
                    Log.d("item size", itemList.size.toString())
                    Log.d("total", total.toString())
                    if (itemList.size == total) {
                        message.what = NODATA
                    } else {
                        itemList.clear()
                        getData(0, 5)
                    }
                    handler.sendMessage(message)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }).start()
        }
        // 初始化数据库, 在程序一运行就创建数据库
        DbConfig.initDb()

        // 取得数据库中得数据
        getDataCount()
        getData(page, pageSize)
    }
    // 获取总数量
    fun getDataCount() {
        val list: MutableList<PlanDb> = LitePal.findAll(PlanDb::class.java)
        total = list.size
        Log.d("list size", total.toString())
    }
    // 加载数据
    private fun getData(page: Int = 0, pageSize: Int = 5) {
        val list: MutableList<PlanDb> = LitePal.select("content", "date", "imgs").where()
            .limit(pageSize).offset(page*pageSize).order("id desc").find(PlanDb::class.java)
        Log.d("geee", list.toString())
        var bitmap: Bitmap?
        for (item in list) {
            bitmap = Utils.base64ToBitmap(item.imgs.split(",")[0])
            itemList.add(PlanItem(bitmap, item.content, item.id))
            if (itemList.size < total) {
                planItemAdapter.loadingStatus = 0
            } else {
                planItemAdapter.loadingStatus = 2
            }
            val message: Message = Message()
            message.what = ENDRFRESH
            handler.sendMessage(message)
        }
    }
    // 重写PlanItemAdapter接口中的方法
    override fun onItemDissmiss(position: Int) {
    }

    override fun onItemMover(fromPosition: Int, toPosition: Int) {

    }
    // 调用adapter中的loadMore上拉加载
    fun loadMore() {
        // 没有更多了
        if (itemList.size == total) {
            return
        }
        // 只有有数据，且不在加载中才会加载数据
        if ((itemList.size < total) && planItemAdapter.loadingStatus==0) {
            planItemAdapter.loadingStatus = 1
            page++
            getData(page, pageSize)
            if (itemList.size < total) {
                planItemAdapter.loadingStatus = 0
            } else {
                planItemAdapter.loadingStatus = 2
            }
        } else {
            return
        }
    }
    // 监听recycler滚动的内部类
    inner class RecyclerListener : RecyclerView.OnScrollListener() {
        private var lastVisibleItem: Int = 0
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            if (dy > 0) {
                floatActionButton.hide()
            } else {
                floatActionButton.show()
            }
            lastVisibleItem = (recyclerView?.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            // 滑动到底
            if (newState == RecyclerView.SCROLL_STATE_IDLE && lastVisibleItem + 1 == recyclerView.adapter?.itemCount) {
                if (swipeRefreshLayout is SwipeRefreshLayout) {
                    if (!swipeRefreshLayout.isRefreshing) {
                        this@MainActivity.loadMore()
                    }
                } else {
                    this@MainActivity.loadMore()
                }
            }
        }
    }
}
