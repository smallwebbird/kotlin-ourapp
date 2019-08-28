package com.example.ourapp.db

import org.litepal.crud.LitePalSupport

class PlanDb(var content: String, var date: String, var imgs: String) : LitePalSupport() {
    // id 为主键， litePal不能自定义主键，只能使用id
    val id: Long = 0
}