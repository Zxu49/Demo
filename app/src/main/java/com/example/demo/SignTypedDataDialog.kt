package com.example.dapp

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import com.coinbase.wallet.crypto.extensions.encryptUsingAES256GCM
import com.example.dapp.utils.getTextInput
import com.example.demo.R


class SignTypedDataDialog(context: Context) : Dialog(context) {

    @SuppressLint("InflateParams")
    class Builder(context: Context) {
        private var sessionID: String ? = null
        private var secret: String ? = null
        private var contentView: View? = null
        private var closeButtonClickListener: View.OnClickListener? = null
        private var sendButtonClickListener: View.OnClickListener? = null

        private val layout: View
        private companion object val dialog: SignTypedDataDialog = SignTypedDataDialog(context)
//        private val dialogContext: Context = context

        init {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            inflater.inflate(R.layout.send_typed_data_view, null).also { layout = it }
            dialog.addContentView(layout, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        }

        fun setCloseButton(listener: View.OnClickListener): Builder {
//            this.singleButtonText = singleButtonText
            this.closeButtonClickListener = listener
            return this
        }

        fun setSendButton(listener: View.OnClickListener): Builder {
//            this.singleButtonText = singleButtonText
            this.sendButtonClickListener = listener
            return this
        }

        fun setSession(sessionID: String?): Builder {
            this.sessionID = sessionID
            return this
        }

        fun setSecret(secret: String?): Builder {
            this.secret = secret
            return this
        }

        fun buildDialog(): SignTypedDataDialog {
            setSendButton{
                SignTypedData()
            }
            showSingleButton()
            layout.findViewById<View>(R.id.send_typedData_close).setOnClickListener(closeButtonClickListener)
            layout.findViewById<View>(R.id.send_typedData_button).setOnClickListener(sendButtonClickListener)

            create()
            return dialog
        }

        private fun create() {
            if (contentView != null) {
                (layout?.findViewById<View>(R.id.content) as LinearLayout).removeAllViews()
                (layout?.findViewById<View>(R.id.content) as LinearLayout)
                    .addView(contentView, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
            }
            dialog.setContentView(layout)
            dialog.setCancelable(true)
            dialog.setCanceledOnTouchOutside(false)
        }

        private fun showSingleButton() {
//            layout.findViewById<View>(R.id.singleButtonLayout).visibility = View.VISIBLE
            layout.findViewById<View>(R.id.SignTypedDataButtonsLayout).visibility = View.VISIBLE
        }

        private fun SignTypedData() {
            val fromAddress: String? = layout?.let {
                getTextInput("SignTyped fromAddress: ",
                    it.findViewById<EditText>(R.id.from_address_input))
            }
            val toAddress: String? = layout?.let {
                getTextInput("SignTyped toAddress: ",
                    it.findViewById<EditText>(R.id.to_address_title))
            }
            val weiValue: String? = layout?.let {
                getTextInput("SignTyped wei: ",
                    it.findViewById<EditText>(R.id.wei_input))
            }
            val dataInput: String? = layout?.let {
                getTextInput("SignTyped data: ",
                    it.findViewById<EditText>(R.id.data_input))
            }
            val nonce: String? = layout?.let {
                getTextInput("SignTyped nonce: ",
                    it.findViewById<EditText>(R.id.nonce_input))
            }
            val gasPrice: String? = layout?.let {
                getTextInput("SignTyped gas Price: ",
                    it.findViewById<EditText>(R.id.gas_price_input))
            }
            val gasLimit: String? = layout?.let {
                getTextInput("SignTyped gas Limit: ",
                    it.findViewById<EditText>(R.id.gas_limit_input))
            }
            val chainId: String? = layout?.let {
                getTextInput("SignTyped chainId: ",
                    it.findViewById<EditText>(R.id.chain_id_input))
            }
            val shouldSubmit: String? = layout?.let {
                getTextInput("SignTyped shouldSubmit: ",
                    it.findViewById<EditText>(R.id.should_submit_input))
            }
//            val intent = Intent(dialogContext, MainActivity::class.java)
//            intent.putExtra("fromAddress", fromAddress);
//            startActivity(dialogContext, intent, null);
            sendSignType()
        }


        fun sendSignType() {
            val jsonString = "{" +
                    "\"type\": \"WEB3_REQUEST\"," + "\"id\": \"13a09f7199d388e9\"," +
                    "\"request\": {" + "\"method\": \"signEthereumTransaction\"," +
                    "\"params\": {" + "\"fromAddress\": \"AddressFrom\"," +
                    "\"toAddress\": \"AddressTo\"," + "\"weiValue\": \"100\"," +
                    "\"data\": \"ZiyangLiuTesting\"," + "\"nonce\": 1," +
                    "\"gasPriceInWei\": \"1\"," + "\"gasLimit\": \"200\"," +
                    "\"chainId\": 888," + "\"shouldSubmit\": false" + "}" +
                    "}," + "\"origin\": \"https://www.usfca.edu\"" + "}"
            //sendRequest(sessionID!!, secret!!,jsonString,"Sent Typed Data to Sign!!!")
            val data = secret?.let { encryptData(jsonString, it) }
            println("The encrypted Data is: $data")
            sessionID?.let {
                if (data != null) {
                    WebsocketClient.sendGetSessionConfigMessage(data)
                }
            }
        }

        private fun encryptData(data: String, secret: String) : String {
            return data.encryptUsingAES256GCM(secret)
        }
    }
}