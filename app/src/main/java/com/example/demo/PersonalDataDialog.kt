package com.example.demo

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

class PersonalDataDialog(context: Context) : Dialog(context) {
    @SuppressLint("InflateParams")
    class Builder(context: Context) {
        private var contentView: View? = null
        private var image: Bitmap? = null
        private var singleButtonText: String? = null
        private var singleButtonClickListener: View.OnClickListener? = null
        private val layout: View
        private val dialog: PersonalDataDialog = PersonalDataDialog(context)

        init {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            inflater.inflate(R.layout.sign_personal_data_view, null).also { layout = it }
            dialog.addContentView(layout, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        }

        fun setSingleButton(singleButtonText: String, listener: View.OnClickListener): Builder {
            this.singleButtonText = singleButtonText
            this.singleButtonClickListener = listener
            return this
        }

        fun createSingleButtonDialog(): PersonalDataDialog {
            showSingleButton()
            layout.findViewById<View>(R.id.singleBtn).setOnClickListener(singleButtonClickListener)
            if (singleButtonText != null) {
                (layout.findViewById<View>(R.id.singleBtn) as TextView).text = singleButtonText
            } else {
                (layout.findViewById<View>(R.id.singleBtn) as TextView).text = "OK"
            }
            create()
            return dialog
        }

        private fun create() {
            if (contentView != null) {
                (layout.findViewById<View>(R.id.content) as LinearLayout).removeAllViews()
                (layout.findViewById<View>(R.id.content) as LinearLayout)
                    .addView(contentView, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
            } else if (image != null) {
                (layout.findViewById<View>(R.id.imageView) as ImageView).setImageBitmap(image)
            }
            dialog.setContentView(layout)
            dialog.setCancelable(true)
            dialog.setCanceledOnTouchOutside(false)
        }

        private fun showSingleButton() {
            layout.findViewById<View>(R.id.singleButtonLayout).visibility = View.VISIBLE
        }
    }

}