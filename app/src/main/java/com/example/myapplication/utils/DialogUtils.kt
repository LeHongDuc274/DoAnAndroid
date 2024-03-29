package com.example.myapplication.utils

import android.content.Context
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import com.example.myapplication.R
import com.example.myapplication.databinding.DialogLogoutBinding
import com.example.myapplication.databinding.DialogRequestStaffLayoutBinding

fun Context.showDialogConfirmLogout(callback: (String) -> Unit) {
    val binding = DialogLogoutBinding.inflate(LayoutInflater.from(this))
    val dialog = AlertDialog.Builder(this, R.style.AlertDialogTheme)
    dialog.apply {
        setView(binding.root)
        setTitle("Đăng xuất")
        setNegativeButton("Huỷ") { d, i ->
        }
        setPositiveButton("Ok") { d, i ->
            callback.invoke(binding.editPassword.text.toString())
        }
        show()
    }
}

fun Context.showDialogResquestStaff(callback: (String) -> Unit) {
    val binding = DialogRequestStaffLayoutBinding.inflate(LayoutInflater.from(this))
    val dialog = AlertDialog.Builder(this, R.style.AlertDialogTheme)
    dialog.apply {
        setView(binding.root)
        setTitle("Gửi yêu cầu")
        setNegativeButton("Huỷ") { d, i ->
        }
        setPositiveButton("Ok") { d, i ->
            callback.invoke(binding.editContent.text.toString())
        }
        show()
    }
}

fun Context.showDialogConfirmEditCategory(old_name: String,callback: (String) -> Unit) {
    val binding = DialogRequestStaffLayoutBinding.inflate(LayoutInflater.from(this))
    val dialog = AlertDialog.Builder(this, R.style.AlertDialogTheme)
    dialog.apply {
        setView(binding.root)
        binding.editContent.hint= "Tên phân loại mới"
        binding.editContent.setText(old_name)
        setTitle("Sửa tên phân loại")
        setNegativeButton("Huỷ") { d, i ->
        }
        setPositiveButton("Ok") { d, i ->
            callback.invoke(binding.editContent.text.toString())
        }
        show()
    }
}