package com.example.myapplication.core.utils

import android.content.Context
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import com.example.myapplication.R
import com.example.myapplication.databinding.DialogLogoutBinding

fun Context.showDialogConfirmLogout(callback: (String) -> Unit) {
    val binding = DialogLogoutBinding.inflate(LayoutInflater.from(this))
    val dialog = AlertDialog.Builder(this, R.style.AlertDialogTheme)
    dialog.apply {
        setView(binding.root)
        setTitle("Log Out")
        setNegativeButton("Cancel") { d, i ->
        }
        setPositiveButton("Ok") { d, i ->
            callback.invoke(binding.editPassword.text.toString())
        }
        show()
    }
}