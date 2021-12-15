package com.example.demo.utils

import android.app.AlertDialog
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.EditText


fun getTextInput(tag: String, editText: EditText): String {
    var textInput = editText.text.toString();
    Log.v(tag, textInput);
    return textInput
}


fun showAlert(context: Context, title: String, result: String) {
    Handler(Looper.getMainLooper()).post {
        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(result)
            .setNegativeButton("Close") { dialog, which ->
                dialog.dismiss()
            }
            .show()
    }
}