package com.example.dapp

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.example.dapp.utils.getTextInput
import com.example.dapp.utils.showAlert
import com.example.demo.Greeter
import com.example.demo.R
import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.methods.response.TransactionReceipt
import org.web3j.protocol.infura.InfuraHttpService
import java.lang.Exception
import java.math.BigInteger
import java.util.concurrent.Future

class SendTransactionDialog(context: Context) : Dialog(context) {

    @SuppressLint("InflateParams")
    class Builder(val context: Context) {
        private val TAG = "SendTransactionDialog"
        // contract address
//    private val contractAddress = "0x8394cDf176A4A52DA5889f7a99c4f7AD2BF59088"
        // endpoint url provided by infura
        private val url = "https://rinkeby.infura.io/v3/01eb8f7b5e514832af8e827c23784d23"
        // web3j infura instance
        private val web3j = Web3j.build(InfuraHttpService(url))
        // gas limit
        private val gasLimit: BigInteger = BigInteger.valueOf(20_000_000_000L)
        // gas price
        private val gasPrice: BigInteger = BigInteger.valueOf(4300000)
        // create credentials w/ your private key
        private val credentials = Credentials.create("f9319fe162c31947c0ca8fd649a536b7ca311b5f210afdc48b62fd7d18ce53e4")

        private var contentView: View? = null
        private var image:Bitmap? = null
        private var singleButtonText: String? = null
        private var closeButtonClickListener: View.OnClickListener? = null
        private var sendButtonClickListener: View.OnClickListener? = null

        private val layout: View
        private val dialog: SendTransactionDialog = SendTransactionDialog(context)

        init {

            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            inflater.inflate(R.layout.send_transaction_view, null).also { layout = it }
            dialog.addContentView(layout, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        }

        fun setImage(bm : Bitmap ): Builder {
            this.image = bm
            return this
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

        @RequiresApi(Build.VERSION_CODES.N)
        fun createDialog(): SendTransactionDialog {
            showSingleButton()
            setSendButton{
                sendTransaction()
            }
            layout.findViewById<View>(R.id.send_transaction_close).setOnClickListener(closeButtonClickListener)
            layout.findViewById<View>(R.id.send_message_button).setOnClickListener(sendButtonClickListener)
            create()
            return dialog
        }

        private fun create() {
            if (contentView != null) {
                (layout.findViewById<View>(R.id.content) as LinearLayout).removeAllViews()
                (layout.findViewById<View>(R.id.content) as LinearLayout)
                    .addView(contentView, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
            }
            dialog.setContentView(layout)
            dialog.setCancelable(true)
            dialog.setCanceledOnTouchOutside(false)
        }

        @RequiresApi(Build.VERSION_CODES.N)
        fun sendTransaction() {
            val contractAddress = getTextInput("Smart contract address: ",
                layout.findViewById<EditText>(R.id.smart_contract_address_input))
            val signMessage = getTextInput("Sign Message: ",
                layout.findViewById<EditText>(R.id.sign_message_text))
            val thread = Thread {
                try {

                    val greeter = Greeter.loadWithCredentials(contractAddress, web3j, credentials, gasLimit, gasPrice)
                    Log.d(TAG, " ${greeter.isValid}")
                    // write to contract "Greeting changed from Jiangfeng Li,  01111!!!(ಠ_ಠ) "
                    //9627151
                    val transactionReceipt: Future<TransactionReceipt>? =
                        greeter.changeGreeting(signMessage).sendAsync()
                    val result = "Successful transaction. Gas used: " +
                            "${transactionReceipt?.get()?.blockNumber}  " +
                            "${transactionReceipt?.get()?.gasUsed}"
                    layout.findViewById<TextView>(R.id.send_prompt).text = result
                    Log.d(TAG, result)
                    showAlert(context,"Send Transaction Status:", result)
                } catch (e: Exception) {
                    Log.d(TAG, "sendTransaction Error: $e.message")
                }
            }

            thread.start()
        }

        private fun showSingleButton() {
            layout.findViewById<View>(R.id.sendTransactionLayout).visibility = View.VISIBLE
        }

    }
}