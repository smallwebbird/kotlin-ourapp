/**
 * @author lizhenghui
 * @date 2019-8-21
 * @description 旅行增加的活动
 * @module PlanAdd
 */
package com.example.ourapp.modules

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.ContentUris
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ourapp.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.io.File
import java.io.IOException
import java.util.*
import android.database.sqlite.SQLiteDatabase
import android.graphics.drawable.ColorDrawable
import android.text.Layout
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.example.ourapp.MainActivity
import com.example.ourapp.common.Utils
import com.example.ourapp.db.PlanDb
import org.w3c.dom.Text

class PlanAdd : AppCompatActivity() {
    private var list: MutableList<PlanAddPhotoItem> = mutableListOf()
    private val weeks: Array<String> = arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    private var date: String = ""
    private val SETDATE: Int = 0
    private lateinit var dateView: TextView
    private var handler: Handler = Handler {
        when(it.what) {
            SETDATE -> {
                dateView.text = date
            }
            else -> {

            }
        }
        false
    }
    // 相册和照相机的按钮
    private lateinit var fromCamera: TextView
    private lateinit var fromAlbum: TextView
    private val FROM_CAMERA: Int = 0
    private val FROM_ALBUM: Int = 1
    private lateinit var bottomSheetDialog: BottomSheetDialog
    private lateinit var imageUri: Uri
    private lateinit var displayPhotoItemAdapter: PlanAddPhotoItemAdapter
    private lateinit var editPlan: EditText
    private lateinit var loadingPlan: ProgressBar
    private lateinit var popupWindow: PopupWindow
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.NoBarTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.module_plan_add)
        // textView
        dateView = findViewById(R.id.date) as TextView

        // toolbar
        var toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar) as androidx.appcompat.widget.Toolbar
        setSupportActionBar(toolbar)
        // 去除label
        supportActionBar?.setDisplayShowTitleEnabled(false)
        // 设置返回键
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        getCurrentDate()

        // 水平的recyclerview
        val recyclerView: RecyclerView = findViewById(R.id.display_photo_recyclerview) as RecyclerView
        val linearLayoutManager: LinearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
        recyclerView.layoutManager = linearLayoutManager
        displayPhotoItemAdapter = PlanAddPhotoItemAdapter(this, list)
        //initDisplay()
        recyclerView.adapter = displayPhotoItemAdapter

        // 顶部的选择图片按钮
        val takePhoto: ImageButton = findViewById(R.id.upload_image) as ImageButton
        takePhoto.setOnClickListener { view: View? -> run {
            bottomSheetDialog = BottomSheetDialog(this)
            val view: View = layoutInflater.inflate(R.layout.bottom_sheet, null)
            bottomSheetDialog.setContentView(view)
            bottomSheetDialog.show()
            fromCamera = view.findViewById(R.id.from_camera) as TextView
            fromAlbum = view.findViewById(R.id.from_album) as TextView
            fromCamera.setOnClickListener { view: View? -> run {
                // 点击拍照按钮的事件
                this.checkTakePhotoPermiss()
            } }
            fromAlbum.setOnClickListener { view: View? -> run {
                // 点击相册的事件
                this.checkAlbumPermiss()
            } }
        } }

        val saveButton: TextView = findViewById(R.id.save_plan) as TextView
        // 保存按钮的监听事件
        saveButton.setOnClickListener { view: View? -> run {
            savePlan()
        } }

        //初始化editInput
        editPlan = findViewById(R.id.edit_plan) as EditText

        //初始化loading
        loadingPlan = findViewById(R.id.plan_loading) as ProgressBar
    }
    // 为了监听返回按钮
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            if (editPlan.text.toString().isEmpty()) {
                backHome()
            }
            showDialog()
        }
        return super.onOptionsItemSelected(item)
    }
    // 返回到home页面
    private fun backHome () {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
    // 显示警告框
    private fun showDialog () {
        val dialog: AlertDialog.Builder = AlertDialog.Builder(this)
        dialog.setTitle("警告")
        dialog.setMessage("当前页面尚未保存，是否退出？")
        dialog.setCancelable(false)
        dialog.setPositiveButton("确定", DialogInterface.OnClickListener{ dialog, which -> run {
            backHome()
        }})
        dialog.setNegativeButton("取消", DialogInterface.OnClickListener{ dialog, which -> run {
            dialog.dismiss()
        }  })
        dialog.show()
    }
    // 申请相机的权限
    private fun checkTakePhotoPermiss() {
        // 6.0之后权限需要使用者自己去决定
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            !== PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            !== PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, Array<String>(2, { Manifest.permission.WRITE_EXTERNAL_STORAGE; Manifest.permission.CAMERA}), FROM_CAMERA)
        } else {
            // 有权限，直接准备打开摄像头
            openCamera()
        }
    }
    // 申请手机相册的权限
    private fun checkAlbumPermiss() {
        if( ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            !== PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, Array<String>( 1, { Manifest.permission.WRITE_EXTERNAL_STORAGE}), FROM_ALBUM)
        } else {
            // 有权限，直接准备打开摄像头
            openAlbum()
        }
    }
    // 打开相册
    fun openAlbum() {
        val intent: Intent = Intent("android.intent.action.GET_CONTENT")
        intent.setType("image/*")
        startActivityForResult(intent, FROM_ALBUM)
    }
    // 打开相机
    private fun openCamera() {
        val outputImage: File = File(externalCacheDir, "output.jpg")
        try {
            if(outputImage.exists()) {
                outputImage.delete()
            }
            outputImage.createNewFile()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        if (Build.VERSION.SDK_INT >= 24) {
            imageUri = FileProvider.getUriForFile(this, "com.example.ourapp.fileprovider", outputImage)
        } else {
            imageUri = Uri.fromFile(outputImage)
        }
        val intent: Intent = Intent("android.media.action.IMAGE_CAPTURE")
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        startActivityForResult(intent, FROM_CAMERA)
    }
    private fun getCurrentDate() {
        val c: Calendar = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)+1
        val day = c.get(Calendar.DAY_OF_MONTH)
        val week = c.get(Calendar.DAY_OF_WEEK)
        date = "${weeks[week-1]}  ${year}/${month}/${day}"
        Log.d("date", date)
        Thread(Runnable {
            try {
                val message: Message = Message()
                message.what = SETDATE
                handler.sendMessage(message)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }).start()
    }
    // 处理从相册读取的图片
    private fun handleImageFromAlbum(data: Intent) {
        val sdkVersion: Int = Build.VERSION.SDK_INT
        if (sdkVersion >= 19) {
            // 系统sdk大于19的
            handleImageOnKitKat(data)
        } else {
            handleImageBeforeKitKat(data)
        }
    }
    // sdk >= 19
    private fun handleImageOnKitKat(data: Intent) {
        val uri: Uri? = data.data
        var imagePath: String? = null
        if (DocumentsContract.isDocumentUri(this, uri)) {
            val docId: String = DocumentsContract.getDocumentId(uri)
            if ("com.android.providers.media.documents".equals(uri?.authority)) {
                val id: String = docId.split(":")[1]
                val selection: String = MediaStore.Images.Media._ID + '=' + id
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection)
            } else if ("com.android.providers.downloads.documents".equals(uri?.authority)) {
                val contentUri: Uri = ContentUris.withAppendedId(
                    Uri.parse("" +
                            "content://downloads/public_downloads"), docId.toLong())
                imagePath = getImagePath(contentUri, null)
            }
        } else if ("content".equals(uri?.scheme, true)) {
            imagePath = getImagePath(uri, null)
        } else if ("file".equals(uri?.scheme, true)) {
            imagePath = uri?.path
        }
        displayImage(imagePath)
    }
    // sdk <19
    private fun handleImageBeforeKitKat(data: Intent) {
        val uri: Uri? = data.data
        val imagePath: String? = getImagePath(uri, null)
        displayImage(imagePath)
    }
    private fun displayImage(imagePath: String?) {
        if (imagePath !== null) {
            val bitmap: Bitmap = BitmapFactory.decodeFile(imagePath)
            list.add(PlanAddPhotoItem(R.drawable.ic_del_image, bitmap))
            displayPhotoItemAdapter.notifyDataSetChanged()
        } else {
            Toast.makeText(this, "获取图像失败", Toast.LENGTH_SHORT).show()
        }
    }
    // 获取图片路径
    private fun getImagePath (uri: Uri?, selection: String?) : String?{
        var path: String? = null
        val cursor: Cursor? = contentResolver.query(uri!!, null, selection, null, null)
        if (cursor !== null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
            }
            cursor.close()
        }
        return path
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            FROM_CAMERA -> {
                if (resultCode == Activity.RESULT_OK) {
                    try {
                        bottomSheetDialog.dismiss()
                        var bitmap: Bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(imageUri))
                        list.add(PlanAddPhotoItem(R.drawable.ic_del_image, bitmap))
                        displayPhotoItemAdapter.notifyDataSetChanged()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
            FROM_ALBUM -> {
                if (resultCode == Activity.RESULT_OK) {
                    bottomSheetDialog.dismiss()
                    handleImageFromAlbum(data!!)
                }
            }
        }
    }
    // 权限的集中处理
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode) {
            FROM_CAMERA -> {
                if(grantResults[0] === PackageManager.PERMISSION_GRANTED) {
                    try {
                        openCamera()
                    } catch (e: IOException) {

                    }
                } else {
                    // 没有权限
                }
            }
            FROM_ALBUM -> {
                if (grantResults[0] === PackageManager.PERMISSION_GRANTED) {
                    openAlbum()
                }
            }
            else -> {

            }
        }
    }
    fun initDisplay () {
        for (item in 1..10) {
            //list.add(DisplayPhoneItem(R.drawable.ic_del_image, R.mipmap.launch))
        }
    }
    // 保存计划的方法
    private fun savePlan () {
        val content = editPlan.text.toString()
        val date = date
        val inputList = list
        val imgs: MutableList<String> = mutableListOf()
        if (content.isEmpty()) {
            Toast.makeText(this, "想法不能为空！", Toast.LENGTH_SHORT).show()
            return
        }
        if (inputList.isEmpty()) {
            Toast.makeText(this, "至少上传一张图片！", Toast.LENGTH_SHORT).show()
            return
        }
        if (inputList.isNotEmpty()) {
            inputList.map { img -> run {
                imgs.add(Utils.bitmapToBase64(img.displayImageId))
            } }
        }
        val saveItem: PlanDb = PlanDb(content, date, imgs.joinToString())
        showPop()
        loadingPlan.visibility = View.VISIBLE
        saveItem.saveAsync().listen { success -> run {
            loadingPlan.visibility = View.GONE
            popupWindow.dismiss()
            backHome()
            Toast.makeText(this, "增加成功", Toast.LENGTH_SHORT).show()
        } }
    }
    private fun showPop() {
        val view = LayoutInflater.from(this).inflate(R.layout.plan_add_popupwindow, null, false)
        popupWindow = PopupWindow(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        popupWindow.setBackgroundDrawable(ColorDrawable(0x00000000));
        popupWindow.contentView = view
        popupWindow.showAsDropDown(view)
    }
}

