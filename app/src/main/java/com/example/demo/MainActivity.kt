package com.example.demo

import WebsocketClient
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.coinbase.wallet.crypto.extensions.encryptUsingAES256GCM
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import PersonalDataDialogFragment
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.coinbase.wallet.core.util.JSON
import com.coinbase.wallet.crypto.extensions.toHexString
import com.coinbase.walletlink.WalletLink
import com.coinbase.walletlink.dtos.*
import com.coinbase.walletlink.models.RequestMethod
import com.example.dapp.SendTransactionDialog
import io.reactivex.rxkotlin.addTo
import java.lang.Exception
import java.net.URL

class MainActivity : AppCompatActivity() , PersonalDataDialogFragment.LoginInputListener, SignTypedDataListener {
    private var builderForCustom: CustomDialog.Builder? = null
    private var mDialog: CustomDialog? = null
    private val sessionIDLength = 32
    private val sessionKeyLength = 64
    private val alphabet: CharRange = ('0'..'9')
    private var sessionID: String = ""
    private var secret: String = ""
    private val metadata = "0x03F6f282373900C2F6CE53B5A9f595b92aC5f5E5"
    private val serialScheduler = Schedulers.single()
    private val disposeBag = CompositeDisposable()
    private var signTypedDataDialogBuiler: SignTypedDataDialog.Builder? = null
    private var signTypedDataDialog: SignTypedDataDialog? = null
    private var sendTransactionbuilder: SendTransactionDialog.Builder? = null
    private var sendTransactionDialog: SendTransactionDialog? = null
    private lateinit var walletLink : WalletLink
    private val notificationUrl = URL("https://walletlink.herokuapp.com")
    private var walletAddress : String  = ""
    private var isCoinBase : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initiateCredentials()
        initWalletLink()
    }

    private fun initiateCredentials(){
        sessionID = List(sessionIDLength) { alphabet.random() }.joinToString("")
        secret = List(sessionKeyLength) { alphabet.random() }.joinToString("")
        println("Session ID:$sessionID")
        println("secret: $secret")
    }

    private fun initWalletLink() {
        walletLink = WalletLink(
            notificationUrl = notificationUrl,
            context = this
        )
    }

    private fun barcodeFormatCode(content: String): Bitmap {
        val barcode = BarcodeFormat.QR_CODE
        val matrix = MultiFormatWriter().encode(content, barcode, 1000, 1000, null)
        return matrix2Bitmap(matrix)
    }

    private fun matrix2Bitmap(matrix: BitMatrix): Bitmap {
        val w = matrix.width
        val h = matrix.height
        val rawData = IntArray(w * h)

        for (i in 0 until w) {
            for (j in 0 until h) {
                var color = Color.WHITE
                if (matrix.get(i, j)) {
                    color = Color.BLACK
                }
                rawData[i + (j * w)] = color
            }
        }

        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(rawData, 0, w, 0, 0, w, h)
        return bitmap
    }

    private fun showSingleButtonDialog(link:String, onClickListener:View.OnClickListener) {
        mDialog = sessionID.let {
            builderForCustom!!
                .setSingleButton("Cancel", onClickListener).setImage(barcodeFormatCode(link)).createSingleButtonDialog()
        }
        mDialog!!.show()
    }

    fun sendSignPersonal(view: android.view.View) {
        val fd = PersonalDataDialogFragment()
        fd.show(supportFragmentManager,"")
    }

    fun startCoinBaseConnection(view: android.view.View){
        isCoinBase = true
        startConnection(view)
    }

    fun startConnection(view: android.view.View) {
        val id = "13a09f7199d39999"
        val appName = "CS690 Team 15 DApp"
        val appLogoUrl = "https://app.compound.finance/images/compound-192.png"
        val origin = "https://www.usfca.edu"
        val jsonRPC = JsonRPCRequestDAppPermissionDataDTO(id = id, request = JsonRPCRequestDAppPermissionDataDTO.Request(
            method = "requestEthereumAccounts",
            params = JsonRPCRequestDAppPermissionDataDTO.Params(appName, appLogoUrl)
        ), origin = origin)
        val data = JSON.toJsonString(jsonRPC).encryptUsingAES256GCM(secret)
        walletLink.sendHostSessionRequest(data, sessionID, secret)
        walletLink.responseObservable
            .observeOn(serialScheduler)
            .subscribe { processIncomingData(it)}
            .addTo(disposeBag)
        walletLink.addressObservable
            .observeOn(serialScheduler)
            .subscribe {
                walletAddress = it
                this.runOnUiThread {
                    Toast.makeText(
                        this,
                        "Fetched wallet address: $walletAddress",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            .addTo(disposeBag)
        val url = "www.walletlink.org"
        val userId = "1"
        var deeplink = "https://${url}?userId=${userId}&secret=${secret}&sessionId=${sessionID}&metadata=${metadata}"
        if(isCoinBase){
            deeplink = "https://www.walletlink.org/#/link?id=$sessionID&secret=$secret&server=https%3A%2F%2Fwww.walletlink.org&v=1"
        }
        builderForCustom = CustomDialog.Builder(this)
        showSingleButtonDialog(deeplink) {
            mDialog!!.dismiss()
        }
        GlobalScope.launch {
            while(true) {
                sessionID.let { WebsocketClient.sendHeartBeatMessage()}
                TimeUnit.SECONDS.sleep(10L)
            }
        }
    }

    private fun processIncomingData (incoming : String) {
        try{
            this.runOnUiThread {
                Toast.makeText(this, incoming, Toast.LENGTH_LONG).show()
            }
        } catch (e : Exception) {
            println(e)
        }
    }

    fun startTransaction(view: android.view.View) {
        val id = "13a09f7199d39999"
        val signedTransaction = "111222333444555"
        val chainId = 3
        val origin = "https://www.usfca.edu"
        val jsonRPC = JsonRPCRequestTransactionDataDTO(id = id, request = Web3RequestTransactionData(method = RequestMethod.SubmitEthereumTransaction, params = SubmitEthereumTransactionParamsRPC(
            signedTransaction,
            chainId
        )
        ), origin = origin)
        val data = JSON.toJsonString(jsonRPC).encryptUsingAES256GCM(secret)
        walletLink.sendStartTransaction(data,sessionID)
    }

    fun cancelRequest(view: android.view.View) {
        val id = "13a09f7199d39999"
        val origin = "https://www.usfca.edu"
        val jsonRPC = JsonRPCRequestCancelDataDTO(id = id, request = Web3RequestCancelData(method = RequestMethod.RequestCanceled), origin = origin)
        val data = JSON.toJsonString(jsonRPC).encryptUsingAES256GCM(secret)
        walletLink.sendCancel(data,sessionID)
    }

    override fun onLoginInputComplete(input: String) {
        val id = "13a09f7199d39999"
        val address = "0x568d46f6a798cd75a9beb60a8f57879043a69c3b"
        val addPrefix = false
        val typedDataJson = "ZiyangLiu"
        val origin = "https://www.usfca.edu"
        var inputString = input
        if (isCoinBase) {
            inputString = input.toByteArray().toHexString()
        }
        val jsonRPC = JsonRPCRequestPersonalDataDTO(id = id, request = Web3RequestPersonalData(method = RequestMethod.SignEthereumMessage, params = SignEthereumMessageParamsRPC(
            inputString,
            address,
            addPrefix,
            typedDataJson
        )), origin = origin)
        val data = JSON.toJsonString(jsonRPC).encryptUsingAES256GCM(secret)
        walletLink.sendSignPersonalData(data, sessionID)
    }

    /**
     * Show SignTypedDataDialog when the user click 'SIGN TYPED DATA' button
     *
     * params
     *  view - view of the context
     */
    fun showSignTypedDataDialog(view: android.view.View) {
        signTypedDataDialogBuiler = SignTypedDataDialog.Builder(this)
        sendSignTypedDataDialog{
            signTypedDataDialog!!.dismiss()
        }
    }


    /**
     * Build SignTypedData Dialog from signTypedData Dialog Builer and set up
     * relevant parameters and listeners. Then show it to the current activity
     *
     * params
     *  onClickListener - OnClickListener that will close the SignTypedData Dialog
     */
    private fun sendSignTypedDataDialog(onClickListener:View.OnClickListener) {
        signTypedDataDialog = sessionID.let {
            secret.let { it1 ->
                signTypedDataDialogBuiler!!
                    .setCloseButton(onClickListener)
                    .setSession(it).setSecret(it1)
                    .setListener(this as SignTypedDataListener)
                    .setWalletLink(walletLink)
                    .buildDialog()
            }
        }
        signTypedDataDialog!!.show()
    }

    /**
     * Show the SendTransaction Dialog when the user click 'SEND TRANSACTION' button
     *
     * params
     *  view - view of the context
     */
    @RequiresApi(Build.VERSION_CODES.N)
    fun showSendTransactionDialog(view: android.view.View) {
        sendTransactionbuilder = SendTransactionDialog.Builder(this)
        sendTransactionDialog(){
            sendTransactionDialog!!.dismiss()
        }
    }

    /**
     * Build sendTransaction Dialog from sendTransaction Dialog Builer and set up
     * relevant parameters and listeners. Then show it to the current activity
     *
     * params
     *  onClickListener - OnClickListener that will close the sendTransaction Dialog
     */
    @RequiresApi(Build.VERSION_CODES.N)
    private fun sendTransactionDialog(onClickListener:View.OnClickListener) {
        sendTransactionDialog = sendTransactionbuilder!!
            .setCloseButton(onClickListener)
            .createDialog()
        sendTransactionDialog!!.show()
    }

    /**
     * Close the SignTypedData Dialog when the user click 'Send' button
     *
     */
    override fun closeSTD() {
        if(signTypedDataDialog?.isShowing == true)
            signTypedDataDialog!!.dismiss()
    }
}