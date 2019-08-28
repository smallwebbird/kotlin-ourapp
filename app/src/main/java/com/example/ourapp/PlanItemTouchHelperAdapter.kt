package com.example.ourapp

interface PlanItemTouchHelperAdapter {
    fun onItemMover(fromPosition: Int, toPosition: Int)
    fun onItemDissmiss(position: Int)
}