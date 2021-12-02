package com.example.dapp.utils

import android.app.AlertDialog
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


fun getTextInput(tag: String, editText: EditText): String {
    var textInput = editText.text.toString();
    Log.v(tag, textInput);
    return textInput
}

fun toastAsync(context: Context, message: String?) {
    println(message)
    Handler(Looper.getMainLooper()).post {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

//    GlobalScope.launch {
//        //TODO("Background processing...")
//        withContext(Dispatchers.Main) {
//            // TODO("Update UI here!")
//        }
//        TODO("Continue background processing...")
//    }
}



fun showAlert(context: Context, title: String, result: String) {
    Handler(Looper.getMainLooper()).post {
        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(result)
//        .setPositiveButton("Approve") { dialog, which ->
//            approveRequest(request.hostRequestId, signData!!)
//        }
            .setNegativeButton("Close") { dialog, which ->
                dialog.dismiss()
            }
            .show()
    }
}

//class HelperFunctions {
//
//    companion object {
//    }
//    val helperFunctions: HelperFunctions = HelperFunctions()
//
//}