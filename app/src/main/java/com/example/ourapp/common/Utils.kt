package com.example.ourapp.common
/**
 * @author lizhenghui
 * @date 2019-8-21
 * @description 工具类，里面包含一些常用的方法
 */

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.IOException

object Utils {
    // 将bitmap转为base64
    fun bitmapToBase64(bitmap: Bitmap) : String {
        val stream: ByteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 10, stream)
        val bitmapBytes: ByteArray = stream.toByteArray()
        return Base64.encodeToString(bitmapBytes, Base64.DEFAULT)
    }
    fun base64ToBitmap(string: String) : Bitmap? {
        //将字符串转换成Bitmap类型
        var bitmap: Bitmap? = null
        try {
            val bitmapArray: ByteArray
            bitmapArray = Base64.decode(string, Base64.DEFAULT)
            bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0, bitmapArray.size)
        } catch (e: IOException) {
            e.printStackTrace();
        }
        return bitmap;
    }
}