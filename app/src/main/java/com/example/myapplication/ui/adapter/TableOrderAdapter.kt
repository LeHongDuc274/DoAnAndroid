package com.example.myapplication.ui.adapter

import android.graphics.Color
import android.graphics.PorterDuff
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.core.api.response.Message
import com.example.myapplication.core.api.response.TableOrdering
import com.example.myapplication.databinding.TableOrderItemBinding

class TableOrderAdapter : RecyclerView.Adapter<TableOrderAdapter.ViewHolder>() {
    private var listTable = mutableListOf<TableOrdering>()
    private var listMessage = mutableListOf<Message>()
    private var itemCallback: ((TableOrdering) -> Unit)? = null
    private var messageCallback: ((TableOrdering) -> Unit)? = null

    inner class ViewHolder(val binding: TableOrderItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun onBind() {
            val table = listTable[layoutPosition]
            binding.tvDisplayName.text = table.display_name
            val hasMessage = listMessage.any {
                it.user_id == table.user_id
            }
            Log.e("tagXX",hasMessage.toString())
            if (hasMessage) {
                binding.ivMessage.visibility = View.VISIBLE
                binding.ivMessage.setColorFilter(
                    Color.parseColor("#FFFFFF"),
                    PorterDuff.Mode.SRC_IN
                )
            } else {
                binding.ivMessage.visibility = View.GONE
            }
            val order = table.order
            if (order == null || order.status == 1) {
                binding.tvState.text = "Empty"
            } else {
                binding.tvState.text = "Using"
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            TableOrderItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val viewholder = ViewHolder(binding)
        initListener(viewholder)
        return viewholder
    }

    private fun initListener(viewholder: TableOrderAdapter.ViewHolder) {
        viewholder.itemView.setOnClickListener {
            itemCallback?.invoke(listTable[viewholder.layoutPosition])
        }
        viewholder.binding.ivMessage.setOnClickListener {
            messageCallback?.invoke(listTable[viewholder.layoutPosition])
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind()
    }

    override fun getItemCount() = listTable.size

    fun setData(list: MutableList<TableOrdering>) {
        listTable.clear()
        listTable.addAll(list)
        notifyDataSetChanged()
    }

    fun setOnItemClick(listener: (TableOrdering) -> Unit) {
        itemCallback = listener
    }

    fun setListMessage(list: MutableList<Message>) {
        Log.e("tagXXX",list.toString())
        listMessage.clear()
        listMessage.addAll(list)
        notifyDataSetChanged()
    }

    fun setOnMessageClick(listener: (TableOrdering) -> Unit){
        messageCallback = listener
    }
}