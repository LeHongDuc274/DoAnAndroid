package com.example.myapplication.ext

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect


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

fun Context.getDrawableByName(name: String): Int =
    resources.getIdentifier(name, "drawable", packageName)

@SuppressLint("Range")
fun Application.getContentType(uri: Uri): String? {
    var type: String? = null
    var cursor = getContentResolver().query(uri, null, null, null, null);
    try {
        if (cursor != null) {
            cursor.moveToFirst()
            type = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.MIME_TYPE));
            cursor.close()
        }
    } catch (e: Exception) {
        e.printStackTrace();
    }
    return type
}

fun Application.AccessToken(): String {
    val sharedPref = getSharedPreferences(
        resources.getString(R.string.shared_file_name), Context.MODE_PRIVATE
    )
    val role = sharedPref.getInt(resources.getString(R.string.key_role), -1)
    return sharedPref.getString(resources.getString(R.string.key_access_token), "").toString()
}

