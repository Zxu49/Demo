package com.example.dapp.utils

/**
 * interface that will listen to the actions of SendTransaction dialog
 *
 */
interface SendTransactionListener {
    fun onSend(result: String)
}