package com.example.myapplication.ui.screen.admin

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.MessageBottomSheetFragmentBinding
import com.example.myapplication.ext.collectFlow
import com.example.myapplication.ui.adapter.MessagesAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BottomSheetMessagesFragment(private val activity: Context) : BottomSheetDialogFragment() {
    private var _binding: MessageBottomSheetFragmentBinding? = null
    private val binding get() = _binding!!
    private var userId = 0
    private val messageAdapter = MessagesAdapter(true)
    private val adminViewModel: AdminViewModel by lazy {
        ViewModelProvider(requireActivity())[AdminViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = MessageBottomSheetFragmentBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initListener()
    }

    private fun initListener() {
        collectFlow(adminViewModel.listMessageRequesting) {
            if (userId != 0) {
                val list = it.filter {
                    it.user_id == userId
                }
                messageAdapter.setData(list.toMutableList())
            } else {
                Log.e("tagIt",it.size.toString())
                messageAdapter.setData(it)
            }
        }
    }

    private fun initViews() {
        val bundle = arguments
        userId = bundle?.getInt("id", 0) ?: 0

        binding.rvMessage.apply {
            adapter = messageAdapter
            layoutManager = LinearLayoutManager(activity)
        }
    }

}