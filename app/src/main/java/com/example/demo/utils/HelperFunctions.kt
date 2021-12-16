package com.example.demo.utils

import android.app.AlertDialog
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.EditText

/**
 * Get the string input text from the EditText
 * params
 *  tag - string of the tag of the activity the editText belongs to
 *  editText - EditText that contains input string value
 *
 * return
 *  String of input text
 */
fun getTextInput(tag: String, editText: EditText): String {
    var textInput = editText.text.toString();
    Log.v(tag, textInput);
    return textInput
}


/**
 * Show Alert containing title and result string
 * params
 *  context - Context of the Alert belongs to
 *  title - string of the title of the Alert
 *  result - string of the result of the Alert
 *
 */
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