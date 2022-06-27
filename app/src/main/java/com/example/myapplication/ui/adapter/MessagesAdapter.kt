package com.example.myapplication.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.core.api.response.Message
import com.example.myapplication.databinding.MessageItemBinding

class MessagesAdapter() : RecyclerView.Adapter<MessagesAdapter.ViewHolder>() {
    private var listMessage = mutableListOf<Message>()
    private var callback: ((Message) -> Unit)? = null

    inner class ViewHolder(val binding: MessageItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun onBind() {
            val message = listMessage[layoutPosition]
            binding.apply {
                tvContent.text = message.content
                tvTableName.text = message.user_name
                tvTime.text = message.created_at
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = MessageItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val holder = ViewHolder(binding)
        initListener(holder)
        return holder
    }

    private fun initListener(holder: MessagesAdapter.ViewHolder) {
        holder.binding.ivNext.setOnClickListener {
            callback?.invoke(listMessage[holder.layoutPosition])
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind()
    }

    override fun getItemCount(): Int = listMessage.size

    fun setData(list: MutableList<Message>) {
        listMessage.clear()
        listMessage.addAll(list)
        notifyDataSetChanged()
    }
}