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
import com.example.demo.utils.getTextInput
import com.example.demo.utils.showAlert
import com.example.demo.Greeter
import com.example.demo.R
import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.methods.response.TransactionReceipt
import org.web3j.protocol.infura.InfuraHttpService
import java.lang.Exception
import java.math.BigInteger
import java.util.concurrent.Future

/**
 * Dialog that allows the users to send greeting message to a smart contract according to its
 * Address
 */
class SendTransactionDialog(context: Context) : Dialog(context) {

    /**
     * Builder of the SendTransaction Dialog, it will set up relevant parameters and listeners
     */
    @SuppressLint("InflateParams")
    class Builder(val context: Context) {
        private val TAG = "SendTransactionDialog"
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
        private var closeButtonClickListener: View.OnClickListener? = null
        private var sendButtonClickListener: View.OnClickListener? = null

        private val layout: View
        private val dialog: SendTransactionDialog = SendTransactionDialog(context)


        /**
         * Initialization function of the SendTransaction Dialog Builder
         */
        init {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            inflater.inflate(R.layout.send_transaction_view, null).also { layout = it }
            dialog.addContentView(layout, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        }

        /**
         * Set the OnClickListener of Close Button when users click it
         *
         * params
         *  listener - OnClickListener that will close the dialog when users click it
         *
         * return
         *  Builder - Builder of the SendTransaction dialog
         */
        fun setCloseButton(listener: View.OnClickListener): Builder {
            this.closeButtonClickListener = listener
            return this
        }

        /**
         * Set the OnClickListener of Send Button when users click it
         *
         * params
         *  listener - OnClickListener that will Send the greeting message to the smart contract
         *  when users click it
         *
         * return
         *  Builder - Builder of the SendTransaction dialog
         */
        fun setSendButton(listener: View.OnClickListener): Builder {
            this.sendButtonClickListener = listener
            return this
        }

        /**
         * Set the relevant configuration of the dialog, attach the listener to the button textviews
         * and create a new SendTransaction dialog from builders
         *
         * return
         *  New SendTransactionDialog
         */
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

        /**
         * Set properties of SendTransaction Dialog
         */
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


        /**
         * Get the contract address and sign message from "smart_contract_address_input" and
         * "sign_message_text" input field and send the sign message to the smart contract
         * specified by the users
         *
         */
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

        /**
         * set the visibility of sendTransactionLayout
         */
        private fun showSingleButton() {
            layout.findViewById<View>(R.id.sendTransactionLayout).visibility = View.VISIBLE
        }

    }
}