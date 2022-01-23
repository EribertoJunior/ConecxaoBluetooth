package com.example.controlebluetootharduino.view.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.controlebluetootharduino.R
import com.google.android.material.card.MaterialCardView

class MainAdapter(var onClickListener: (nameDevice:String) -> Unit) : RecyclerView.Adapter<MainAdapter.ViewHolder>() {

    private var listNameDevice = arrayListOf<String>()

    inner class ViewHolder(var view: View) : RecyclerView.ViewHolder(view) {

        fun vincula(nameDevice: String) {
            view.findViewById<TextView>(R.id.tv_nome_device).apply {
                text = nameDevice
            }

            view.findViewById<MaterialCardView>(R.id.card_item).setOnClickListener {
                onClickListener(nameDevice)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_scan_device, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.vincula(listNameDevice[position])
    }

    override fun getItemCount() = listNameDevice.size

    fun dataSet(listNames: List<String>?) {
        listNames?.let {
            listNameDevice.addAll(it)
            notifyDataSetChanged()
        }
    }
}