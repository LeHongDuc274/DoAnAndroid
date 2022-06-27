package com.example.myapplication.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.R
import com.example.myapplication.core.Role
import com.example.myapplication.core.USER_EXTRA_KEY
import com.example.myapplication.core.model.User
import com.example.myapplication.core.utils.GsonUtils
import com.example.myapplication.databinding.FragmentUserFormBinding
import com.example.myapplication.ui.admin.ProductFormFragment.Companion.NEW_MODE
import com.example.myapplication.ui.admin.ProductFormFragment.Companion.EDIT_MODE
import com.example.myapplication.viewmodel.AdminViewModel
import com.example.myapplication.wiget.BaseDialogFragment

class UserFormFragment : BaseDialogFragment(R.layout.fragment_user_form) {
    private var _binding: FragmentUserFormBinding? = null
    private val binding get() = _binding!!
    private var mode = NEW_MODE
    private var user: User? = null
    private val adminViewmodel: AdminViewModel by lazy {
        ViewModelProvider(requireActivity())[AdminViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUserFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initListener()
    }

    private fun initListener() {
        binding.icToolBar.tvBack.setOnClickListener {
            dismiss()
        }
        binding.tvEdit.setOnClickListener {
            if (mode == NEW_MODE) {
                createUser()
            } else {
                editUser()
            }
        }
    }

    private fun editUser() {
        val loginId = binding.edtLoginId.text.toString()
        val displayName = binding.edtDisplayName.text.toString()
        val status = if (binding.swStatus.isChecked) "0" else "1"
        val checkedId = binding.rgRole.checkedRadioButtonId
        val role = when (checkedId) {
            binding.rbTable.id -> "3"
            binding.rbStaff.id -> "1"
            binding.rbKitchen.id -> "2"
            else -> ""
        }
        if (loginId.isBlank() || displayName.isBlank() || status.isBlank() || role.isBlank()) {
            Toast.makeText(requireActivity(), "Value cann't be blank", Toast.LENGTH_LONG).show()
        } else {
            adminViewmodel.editUser(user!!.id, displayName, status, role) { b, str, user ->
                Toast.makeText(requireActivity(), str, Toast.LENGTH_LONG).show()
                if (b) dismiss()
            }
        }
    }

    private fun createUser() {
        val loginId = binding.edtLoginId.text.toString()
        val displayName = binding.edtDisplayName.text.toString()
        val password = binding.edtPassword.text.toString()
        val passwordConfirm = binding.edtPasswordConfirm.text.toString()
        val status = if (binding.swStatus.isChecked) "0" else "1"
        val checkedId = binding.rgRole.checkedRadioButtonId
        val role = when (checkedId) {
            binding.rbTable.id -> "3"
            binding.rbStaff.id -> "1"
            binding.rbKitchen.id -> "2"
            else -> ""
        }
        if (loginId.isBlank() || displayName.isBlank() || password.isBlank() || role.isBlank() || !(password.equals(
                passwordConfirm
            ))
        ) {
            Toast.makeText(requireActivity(), "Value cann't be blank", Toast.LENGTH_LONG).show()
        } else {
            adminViewmodel.createUser(
                loginId,
                displayName,
                password,
                passwordConfirm,
                status,
                role
            ) { b, str, _ ->
                Toast.makeText(requireActivity(), str, Toast.LENGTH_LONG).show()
                if (b) dismiss()
            }
        }
    }

    private fun initViews() {
        arguments?.let {
            mode = EDIT_MODE
            user =
                GsonUtils.getGsonParser().fromJson(it.getString(USER_EXTRA_KEY), User::class.java)
            binding.edtLoginId.setText(user!!.login_id)
            binding.edtLoginId.isClickable = false
            binding.edtLoginId.isFocusable = false
            binding.edtDisplayName.setText(user!!.display_name)
            binding.swStatus.isChecked = user!!.status == 0
            binding.edtPassword.visibility = View.GONE
            binding.edtPasswordConfirm.visibility = View.GONE
            binding.tvPassword.visibility = View.GONE
            binding.tvPasswordConfirm.visibility = View.GONE
            binding.tvEdit.text = "Edit"
            when (user!!.role) {
                Role.STAFF.code -> binding.rgRole.check(binding.rbStaff.id)
                Role.TABLE.code -> binding.rgRole.check(binding.rbTable.id)
                Role.KITCHEN.code -> binding.rgRole.check(binding.rbKitchen.id)
            }
        }
        binding.icToolBar.tvTitle.text = "User"
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}