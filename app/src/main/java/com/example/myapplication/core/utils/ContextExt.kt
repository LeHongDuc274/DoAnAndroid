package com.example.myapplication.ext

import android.app.Application
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.ui.login.LoginActivity
import com.example.myapplication.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import okhttp3.MediaType
import okhttp3.RequestBody

fun <T> AppCompatActivity.collectFlow(targetFlow: Flow<T>, collectBlock: ((T) -> Unit)) {
    lifecycleScope.launchWhenStarted {
        targetFlow.collect {
            collectBlock(it)
        }
    }
}

fun <T> Fragment.collectFlow(targetFlow: Flow<T>, collectBlock: ((T) -> Unit)) {
    lifecycleScope.launchWhenStarted {
        targetFlow.collect {
            collectBlock(it)
        }
    }
}

fun AppCompatActivity.gotoLogin() {
    val intent = Intent(this, LoginActivity::class.java)
    intent.flags =
        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    startActivity(intent)
}

fun Context.getDrawableByName(name: String): Int =
    resources.getIdentifier(name, "drawable", packageName)

fun Application.AccessToken(): String {
    val sharedPref = getSharedPreferences(
        resources.getString(R.string.shared_file_name), Context.MODE_PRIVATE
    )
    return sharedPref.getString(resources.getString(R.string.key_access_token), "").toString()
}

fun Application.DisplayName(): String {
    val sharedPref = getSharedPreferences(
        resources.getString(R.string.shared_file_name), Context.MODE_PRIVATE
    )
    return sharedPref.getString(resources.getString(R.string.key_display_name), "").toString()
}

fun Application.UserId(): Int {
    val sharedPref = getSharedPreferences(
        resources.getString(R.string.shared_file_name), Context.MODE_PRIVATE
    )
    return sharedPref.getInt(resources.getString(R.string.key_id), -1)
}

fun createRequestBody(str: String): RequestBody {
    return RequestBody.create(MediaType.parse("text/plain"), str)
}

fun Context.showToast(mess: String) {
    Toast.makeText(this, mess, Toast.LENGTH_SHORT).show()
}

