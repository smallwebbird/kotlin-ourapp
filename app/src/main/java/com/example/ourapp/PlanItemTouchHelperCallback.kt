package com.example.ourapp

import android.content.ClipData
import android.graphics.Canvas
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import java.nio.channels.FileLock

class PlanItemTouchHelperCallback(val adapter: PlanItemTouchHelperAdapter) : ItemTouchHelper.Callback() {
    // 获取侧滑菜单的宽度
    private var menuBtn: Int = 0
    // 当前滑动距离
    private var scrollDistance: Float = 0f
    // 是否可以向左侧滑动
    private var isNeedRecover: Boolean = true
    // 是否可以向左滑动
    private var isCanScrollLeft: Boolean = false
    // 是否可以向又滑动
    private var isCanScrollRight: Boolean = false
    // 是否可以长按滑动拖动
    override fun isLongPressDragEnabled(): Boolean {
        return false
    }
    // 是否可以滑动
    override fun isItemViewSwipeEnabled(): Boolean {
        return true
    }
    // 滑动时的回调
    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
       adapter.onItemDissmiss(viewHolder.adapterPosition)
    }
    // 拖拽时的回调
    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
    // 当用户拖拽或者滑动item时滑动拖拽的方向
    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        val dragFlags = ItemTouchHelper.DOWN or  ItemTouchHelper.UP
        val swipeFlags = ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT
        return makeMovementFlags(dragFlags, swipeFlags)
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        // 只对侧滑进行处理
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            // 获取menu按钮的宽度
            if (menuBtn <= 0) {
                menuBtn = getMenuBtnWidth(viewHolder)
            }
            val currentScroll: Int = viewHolder.itemView.scrollX
            // 向左滑动
            if (dX < 0 && isCanScrollLeft && currentScroll <= menuBtn) {
                var newDX: Float = if (Math.abs(dX) <= menuBtn) dX else -menuBtn.toFloat()
                if (!isNeedRecover) {
                    // 当滑动结束的时候调用
                    var newScroll: Int = menuBtn + newDX.toInt()
                    newScroll = if (newScroll <= currentScroll) currentScroll else newScroll
                    viewHolder.itemView.scrollTo(newScroll, 0)
                } else {
                    // 过程中调用
                    viewHolder.itemView.scrollTo(-newDX.toInt(), 0)
                    scrollDistance = newDX
                }
            } else if (dX > 0 && isCanScrollLeft) {
                // 可以左滑的情况下向右滑
                viewHolder.itemView.scrollTo(0, 0)
                scrollDistance = 0f
            } else if (dX > 0 && isCanScrollRight && currentScroll >= 0) {
                if (!isNeedRecover) {
                    var newDX: Float = if (Math.abs(dX) <= Math.abs(currentScroll)) dX else currentScroll.toFloat()
                    viewHolder.itemView.scrollTo(newDX.toInt(), 0)
                } else {
                    var newDX: Float = if (Math.abs(dX) <= menuBtn) dX else menuBtn.toFloat()
                    viewHolder.itemView.scrollTo(menuBtn - newDX.toInt(), 0)
                    scrollDistance = newDX
                }
            } else if (dX < 0 && isCanScrollRight) {
                viewHolder.itemView.scrollTo(menuBtn, 0)
                scrollDistance = menuBtn.toFloat()
            }
        } else {
            // 拖拽的状态下不做任何改变
            super.onChildDraw(c,recyclerView,viewHolder,dX,dY,actionState,isCurrentlyActive);
        }
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        // 滑动过程中
        if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
            isNeedRecover = true;
            scrollDistance = 0f;
            isCanScrollLeft = false;
            isCanScrollRight = false;
            if (viewHolder!!.itemView.scrollX > 0) {
                isCanScrollRight = true
            } else {
                isCanScrollLeft = true
            }
        } else {
            // 滑动结束时
            if (Math.abs(scrollDistance) >= menuBtn/2) {
                isNeedRecover = false
            }
        }
        super.onSelectedChanged(viewHolder, actionState)
    }
    fun getMenuBtnWidth(viewHolder: RecyclerView.ViewHolder) : Int {
        val viewGroup: ViewGroup = viewHolder.itemView as ViewGroup
        return viewGroup.getChildAt(1).layoutParams.width
    }
    // 设置滑动速度不让它进入onSwipe
    override fun getSwipeEscapeVelocity(defaultValue: Float): Float {
        return defaultValue * 100
    }

    override fun getMoveThreshold(viewHolder: RecyclerView.ViewHolder): Float {
        return 1.5f
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        // 滑动距离超过编辑按钮一半时，和没有超过一半时的情况
        if (viewHolder.itemView.scrollX > menuBtn / 3) {
            viewHolder.itemView.scrollTo(menuBtn, 0)
        } else {
            viewHolder.itemView.scrollTo(0, 0)
        }
    }
}