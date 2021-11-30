package com.example.demo


import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.coinbase.wallet.core.extensions.unwrap
import com.coinbase.wallet.core.util.Optional
import com.coinbase.wallet.http.connectivity.Internet
import com.coinbase.walletlink.WalletLink
import com.coinbase.walletlink.exceptions.WalletLinkException
import com.coinbase.walletlink.models.*
import io.github.g00fy2.quickie.QRResult
import io.github.g00fy2.quickie.ScanQRCode

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import java.net.URL
import java.util.concurrent.ConcurrentHashMap

@SuppressLint("StaticFieldLeak")


class MainActivity : AppCompatActivity() {
    // Call back url
    private val notificationUrl = URL("https://walletlink.herokuapp.com")

    // Hardcode Const val for test related parameters
    private val testAddress = "0x03F6f282373900C2F6CE53B5A9f595b92aC5f5E5"
    private val testUserId = "123"

    // Object that GC all observable convert into destroyable once it finish event
    private var disposeBag = CompositeDisposable()

    // Wallet related parameters
    private var walletLink : WalletLink? = null
    private var session : Session? = null

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initWalletLink(notificationUrl)
    }

    override fun onDestroy() {
        super.onDestroy()
        session?.let { walletLink?.unlink(it) }
        walletLink?.disconnect()
    }

    /**
     *  Init wallet-link instance - should be singleton
     */
    private fun initWalletLink(notificationUrl : URL) {
        val intentFilter = IntentFilter().apply { addAction(ConnectivityManager.CONNECTIVITY_ACTION) }
        this.registerReceiver(Internet, intentFilter)
        Internet.startMonitoring()

        walletLink = WalletLink(
            notificationUrl = notificationUrl,
            context = this
        )
    }

    /**
     *  Helper function for handle host request from Dapp generated QR code
     */
    private fun handleResult(result: QRResult) {
        val alertDialogBuilder = AlertDialog.Builder(this)
        var message = "Click the link means you allow this dapp access your public key"
        alertDialogBuilder.setTitle("Connect this site")

        when (result) {
            is QRResult.QRSuccess -> {
                alertDialogBuilder.setPositiveButton("Link") { dialog, which ->
                    println("result.content.rawValue : ${result.content.rawValue}")
                    toastAsync("Connect Success")
                }
            }
            is QRResult.QRUserCanceled -> message = "User canceled scanning"
            is QRResult.QRMissingPermission -> message = "Missing permission"
            is QRResult.QRError -> message = "${result.exception.javaClass.simpleName}: ${result.exception.localizedMessage}"
        }
        alertDialogBuilder.setMessage(message)
        alertDialogBuilder.setNegativeButton("Cancel") { dialog, which ->
            toastAsync("Connect cancel")
        }
        alertDialogBuilder.show()
    }



    /**
     *  Approve a given transaction/message signing request
     */
    private fun approveRequest(hostRequestId : HostRequestId, signedData : ByteArray) {
        walletLink!!.approve(hostRequestId, signedData)
            .subscribeBy(onSuccess = {
                print("Received request approval")
            }, onError = { error ->
                error("Failed to receive request approval")
            })
            .addTo(disposeBag)
    }

    // Reject transaction/message/EIP-1102 request
    private fun rejectRequest(hostRequestId : HostRequestId) {
        walletLink!!.reject(hostRequestId)
            .subscribeBy(onSuccess = {
                print("Reject request approval")
            }, onError = { error ->
                print("Failed to receive request approval")
            })
            .addTo(disposeBag)
    }

    private fun connectWalletLink() {
        val metadata : ConcurrentHashMap<ClientMetadataKey, String> = ConcurrentHashMap()
        metadata[ClientMetadataKey.EthereumAddress] = testAddress

        walletLink!!.connect(
            userId = testUserId,
            metadata = metadata
        )
    }

    private fun toastAsync(message: String?) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Helper function for listen dapp request from wallet link server
     */
    private fun listenRequestsFromSocket() {
        var requestTitle : String? = null
        var requestContext : String? = null
        val signData = "[\"0x03F6f282373900C2F6CE53B5A9f595b92aC5f5E5\"]".toByteArray(Charsets.UTF_8)
        var dialogBuilder = AlertDialog.Builder(this)

        walletLink!!.requestsObservable
            .observeOn(AndroidSchedulers.mainThread())
            .map { Optional(it) }
            .onErrorReturn { Optional(null) }
            .unwrap()
            .subscribe { request ->
                when (request) {
                    is HostRequest.DappPermission -> {
                        requestTitle = "A Dapp Permission coming"
                        requestContext = "${request.dappName} want to connect your wallet"
                    }

                    is HostRequest.SignMessage -> {
                        requestTitle = "Message from ${request.dappName}"
                        requestContext = request.message
                    }

                    is HostRequest.SignAndSubmitTx -> {
                        requestTitle = "Sign the Transaction on ${request.dappName}?"
                        requestContext =
                            "Detail of Transaction\n" +
                                    "from: ${request.fromAddress},\n" +
                                    "to: ${request.toAddress},\n" +
                                    "data: ${request.data.toString(Charsets.UTF_8)},\n" +
                                    "gasPrice: ${request.gasPrice},\n" +
                                    "gasLimit: ${request.gasLimit},\n" +
                                    "chainId : ${request.chainId} \n"
                    }

                    is HostRequest.SubmitSignedTx -> {
                        requestTitle = "Submit the Transaction on ${request.dappName}?"
                        requestContext =
                            "tx : ${request.signedTx.toString(Charsets.UTF_8)} \n" +
                                    "chainId : ${request.chainId} \n"
                    }

                    is HostRequest.RequestCanceled -> {
                        requestTitle = "Cancel the Transaction on ${request.dappName}?"
                        requestContext = "Transaction Request Cancel"
                    }
                }

                dialogBuilder
                    .setTitle(requestTitle)
                    .setMessage(requestContext)

                AlertDialog.Builder(this)
                    .setTitle(requestTitle)
                    .setMessage(requestContext)
                if (request is HostRequest.RequestCanceled) {
                    dialogBuilder
                        .setPositiveButton("OK"){ dialog, which ->
                            walletLink!!.markAsSeen(requestIds = listOf(request.hostRequestId))
                                .subscribeBy(onSuccess = {
                                    toastAsync("The request had been canceled")
                                }, onError = {
                                    toastAsync("The request fail to be canceled")
                                }).addTo(disposeBag)
                        }.setNegativeButton("Don't cancel") { dialog, which ->
                            rejectRequest(request.hostRequestId)
                        }
                } else {
                    dialogBuilder
                        .setPositiveButton("Approve") { dialog, which ->
                            approveRequest(request.hostRequestId, signData!!)
                        }.setNegativeButton("Reject") { dialog, which ->
                            rejectRequest(request.hostRequestId)
                        }
                }
                dialogBuilder.show()
            }
            .addTo(disposeBag)
    }
}
