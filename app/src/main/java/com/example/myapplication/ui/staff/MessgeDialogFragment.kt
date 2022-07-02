package com.example.myapplication.ui.staff

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
import com.example.myapplication.databinding.MessageBottomSheetFragmentBinding
import com.example.myapplication.ext.collectFlow
import com.example.myapplication.ui.adapter.MessagesAdapter
import com.example.myapplication.viewmodel.KitchenViewModel
import com.example.myapplication.wiget.BaseDialogFragment

class MessgeDialogFragment : BaseDialogFragment(R.layout.message_bottom_sheet_fragment) {
    private var _binding: MessageBottomSheetFragmentBinding? = null
    private val binding get() = _binding!!
    private var userId = 0
    private val messageAdapter = MessagesAdapter()
    private val kitchenVM: KitchenViewModel by lazy {
        ViewModelProvider(requireActivity())[KitchenViewModel::class.java]
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
        collectFlow(kitchenVM.listMessageRequesting) {
            if (userId != 0) {
                val list = it.filter {
                    it.user_id == userId
                }
                messageAdapter.setData(list.toMutableList())
            } else {
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
        messageAdapter.setCallback {
            kitchenVM.doRequest(it)
        }
    }
}