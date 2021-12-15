package com.example.demo

import android.util.Log
import android.widget.EditText
import android.widget.TextView
import com.example.dapp.utils.getTextInput
import com.example.dapp.utils.showAlert
import junit.framework.Assert.assertNotNull
import org.junit.Assert
import org.junit.Test
import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.methods.response.TransactionReceipt
import org.web3j.protocol.infura.InfuraHttpService
import java.lang.Exception
import java.math.BigInteger
import java.util.concurrent.Future

/**
 * Local unit test for SendTransactionDialog, which will test whether user can send greeting
 * message to the smart contract.
 *
 */
class SendTransactionTest {
    private val contractAddress = "0x8394cDf176A4A52DA5889f7a99c4f7AD2BF59088"
    private val TAG = "SendTransactionTest"
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

    @Test
    fun can_send_transaction() {
        val signMessage = "This is can_send_transaction function of SendTransactionTest Kotlin " +
                "file in Demo repo!!!"
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
                Log.d(TAG, result)
                assertNotNull(transactionReceipt?.get()?.blockNumber)
                assertNotNull(transactionReceipt?.get()?.gasUsed)
            } catch (e: Exception) {
                Log.d(TAG, "sendTransaction Error: $e.message")
            }
        }

        thread.start()
    }

}