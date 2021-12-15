// Copyright (c) 2018-2019 Coinbase, Inc. <https://coinbase.com/>
// Licensed under the Apache License, version 2.0
package com.example.demo
import android.content.Context
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.util.Log
import android.widget.Toast
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.coinbase.wallet.core.extensions.base64EncodedString
import com.coinbase.wallet.core.util.JSON
import com.coinbase.wallet.crypto.ciphers.AES256GCM
import com.coinbase.wallet.crypto.extensions.encryptUsingAES256GCM
import com.coinbase.wallet.http.connectivity.Internet
import com.coinbase.walletlink.WalletLink
import com.coinbase.walletlink.dtos.JsonRPCRequestDAppPermissionDataDTO
import com.coinbase.walletlink.models.ClientMetadataKey
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import java.net.URL
import java.util.concurrent.CountDownLatch

@RunWith(AndroidJUnit4::class)
class DAppTest {
    @Test
    fun testSendHostSession() {
        val appContext = ApplicationProvider.getApplicationContext<Context>()
        val walletLink = WalletLink(
            notificationUrl = URL("https://walletlink.herokuapp.com"),
            context = appContext
        )
        val id = "13a09f7199d39999"
        val appName = "CS690 Team 15 DApp"
        val appLogoUrl = "https://app.compound.finance/images/compound-192.png"
        val origin = "https://www.usfca.edu"
        val jsonRPC = JsonRPCRequestDAppPermissionDataDTO(id = id, request = JsonRPCRequestDAppPermissionDataDTO.Request(
            method = "requestEthereumAccounts",
            params = JsonRPCRequestDAppPermissionDataDTO.Params(appName, appLogoUrl)
        ), origin = origin)
        val sessionID = "35245454007279607403463727553499"
        val secret = "6211949012973140237169099175835859749177568667191566915960143749"
        val data = JSON.toJsonString(jsonRPC).encryptUsingAES256GCM(secret)
        val serialScheduler = Schedulers.single()
        walletLink.sendHostSessionRequest(data, sessionID, secret)
        walletLink.responseObservable
            .observeOn(serialScheduler)
            .subscribe {
                Log.i("MSG","wallet link connected!!")
            }
    }

    @Test
    fun testFetchAddress() {
        val appContext = ApplicationProvider.getApplicationContext<Context>()
        val walletLink = WalletLink(
            notificationUrl = URL("https://walletlink.herokuapp.com"),
            context = appContext
        )
        val id = "13a09f7199d39999"
        val appName = "CS690 Team 15 DApp"
        val appLogoUrl = "https://app.compound.finance/images/compound-192.png"
        val origin = "https://www.usfca.edu"
        val jsonRPC = JsonRPCRequestDAppPermissionDataDTO(id = id, request = JsonRPCRequestDAppPermissionDataDTO.Request(
            method = "requestEthereumAccounts",
            params = JsonRPCRequestDAppPermissionDataDTO.Params(appName, appLogoUrl)
        ), origin = origin)
        val sessionID = "35245454007279607403463727553499"
        val secret = "6211949012973140237169099175835859749177568667191566915960143749"
        val data = JSON.toJsonString(jsonRPC).encryptUsingAES256GCM(secret)
        val serialScheduler = Schedulers.single()
        walletLink.sendHostSessionRequest(data, sessionID, secret)
        walletLink.addressObservable
            .observeOn(serialScheduler)
            .subscribe {
                println(it)
            }
    }

    @Test
    fun encryptionDecryption() {
        val data = "Needs encryption".toByteArray()
        val key = "c9db0147e942b2675045e3f61b247692".toByteArray()
        val iv = "123456789012".toByteArray()
        val (encryptedData, authTag) = AES256GCM.encrypt(data, key, iv)

        val decryptedData = AES256GCM.decrypt(encryptedData, key, iv, authTag)

        Assert.assertEquals(data.base64EncodedString(), decryptedData.base64EncodedString())
    }

    @Test
    fun testGenerateCredentials(){
        val alphabet: CharRange = ('0'..'9')
        var sessionID = List(32) { alphabet.random() }.joinToString("")
        var secret = List(64) { alphabet.random() }.joinToString("")
        println("Session ID:$sessionID")
        println("secret: $secret")
    }
}
