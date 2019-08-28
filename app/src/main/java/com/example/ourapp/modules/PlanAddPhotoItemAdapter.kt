package com.example.ourapp.modules

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.ourapp.R

class PlanAddPhotoItemAdapter(private var context: Context, private var list: MutableList<PlanAddPhotoItem>) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    inner class ItemHolder(view: View) : RecyclerView.ViewHolder(view) {
        val displayImage: ImageView = view.findViewById(R.id.display_image)
        val delImage: ImageView = view.findViewById(R.id.del_image)
    }
    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.display_photo_item, p0, false)
        val itemHolder: ItemHolder = ItemHolder(view)
        return itemHolder
    }

    override fun onBindViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {
        val itemHolder: ItemHolder = p0 as ItemHolder
        itemHolder.displayImage.setImageBitmap(list[p1].displayImageId)
        itemHolder.delImage.setImageResource(list[p1].delImageId)
        // 在这里可以进行组件的监听
        itemHolder.delImage.setOnClickListener { view: View? -> run {
            list.removeAt(p1)
            notifyDataSetChanged()
        } }
    }
    override fun getItemCount(): Int {
        return list.size
    }
}