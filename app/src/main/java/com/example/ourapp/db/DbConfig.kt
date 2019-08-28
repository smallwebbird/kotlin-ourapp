package com.example.ourapp.db

import android.database.sqlite.SQLiteDatabase
import org.litepal.LitePal

/**
 * @author lizhenghui
 * @date 2019-8-21
 * @description 数据库的一些配置文件，数据库的初始化
 */

object DbConfig {
    var db: SQLiteDatabase? = null
    fun initDb() {
        db = LitePal.getDatabase()
    }
}