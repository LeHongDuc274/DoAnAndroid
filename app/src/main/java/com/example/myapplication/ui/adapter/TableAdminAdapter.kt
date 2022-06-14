package com.example.myapplication.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.core.model.User
import com.example.myapplication.databinding.TableItemBinding

class TableAdminAdapter() : RecyclerView.Adapter<TableAdminAdapter.ViewHolder>() {

    private var listTable = mutableListOf<User>()
    private var onEdit: ((User) -> Unit)? = null
    private var onItemClick: ((User) -> Unit)? = null

    inner class ViewHolder(val binding: TableItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun onBind() {
            binding.tvName.text = listTable[layoutPosition].display_name
            binding.tvStatus.text = "Status : " + listTable[layoutPosition].status
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = TableItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val holder = ViewHolder(binding)
        initListenner(holder, binding)
        return holder
    }

    private fun initListenner(holder: ViewHolder, binding: TableItemBinding) {
        binding.tvEdit.setOnClickListener {
            onEdit?.invoke(listTable[holder.layoutPosition])
        }
        binding.root.setOnClickListener {
            onItemClick?.invoke(listTable[holder.layoutPosition])
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind()
    }

    override fun getItemCount() = listTable.size

    fun setData(list : List<User>){
        listTable.clear()
        listTable.addAll(list)
        notifyDataSetChanged()
    }

    fun setOnEdit(callback: (User) -> Unit){
        onEdit = callback
    }

    fun setOnItemClick(callback: (User) -> Unit){
        onItemClick = callback
    }
}