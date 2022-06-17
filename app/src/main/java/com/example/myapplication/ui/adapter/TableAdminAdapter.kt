package com.example.myapplication.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.core.model.User
import com.example.myapplication.databinding.TableItemBinding

class TableAdminAdapter() : RecyclerView.Adapter<TableAdminAdapter.ViewHolder>() {

    private var listTable = mutableListOf<User>()
    private var onItemClick: ((User) -> Unit)? = null

    inner class ViewHolder(val binding: TableItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun onBind() {
            if (listTable[layoutPosition].id != -1) {
                binding.tvName.visibility = View.VISIBLE
                binding.tvStatus.visibility = View.VISIBLE
                binding.tvEdit.visibility = View.VISIBLE
                binding.tvAdd.visibility = View.INVISIBLE
                binding.tvName.text = listTable[layoutPosition].display_name
                binding.tvStatus.text = "Status : " + listTable[layoutPosition].status
            } else {
                binding.tvName.visibility = View.INVISIBLE
                binding.tvStatus.visibility = View.INVISIBLE
                binding.tvEdit.visibility = View.INVISIBLE
                binding.tvAdd.visibility = View.VISIBLE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = TableItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val holder = ViewHolder(binding)
        initListenner(holder, binding)
        return holder
    }

    private fun initListenner(holder: ViewHolder, binding: TableItemBinding) {
        binding.root.setOnClickListener {
            onItemClick?.invoke(listTable[holder.layoutPosition])
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind()
    }

    override fun getItemCount() = listTable.size

    fun setData(list: List<User>) {
        listTable.clear()
        listTable.add(User(-1, -1, "", "", -1))
        listTable.addAll(list)
        notifyDataSetChanged()
    }

    fun setOnItemClick(callback: (User) -> Unit) {
        onItemClick = callback
    }
}