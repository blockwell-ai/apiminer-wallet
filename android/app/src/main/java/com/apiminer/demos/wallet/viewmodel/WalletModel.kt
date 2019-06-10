package com.apiminer.demos.wallet.viewmodel

import androidx.lifecycle.ViewModel
import android.util.Log
import com.apiminer.demos.wallet.api.ApiClient
import com.apiminer.demos.wallet.api.BalanceChannel
import com.apiminer.demos.wallet.api.TransferStatusChannel
import com.apiminer.demos.wallet.api.TransfersChannel
import com.apiminer.demos.wallet.data.DataStore

class WalletModel(client: ApiClient) : ViewModel() {
    val balance by lazy { BalanceChannel(client) }
    val transfers by lazy { TransfersChannel(client) }

    // Save a reference to the Lazy instance, since transfer status isn't always initialized
    private val transferStatusLazy = lazy { TransferStatusChannel(client) }
    val transferStatus by transferStatusLazy

    override fun onCleared() {
        Log.d("WalletModel", "Cancelling channels")
        balance.cancel()
        transfers.cancel()

        if (transferStatusLazy.isInitialized()) {
            transferStatus.cancel()
        }
    }

    fun getPendingTransfer() = DataStore.pendingTransfer
}
