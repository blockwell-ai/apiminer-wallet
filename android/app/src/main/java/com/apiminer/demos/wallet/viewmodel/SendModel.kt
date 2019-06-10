package com.apiminer.demos.wallet.viewmodel

import androidx.lifecycle.ViewModel
import com.apiminer.demos.wallet.api.ApiClient
import com.apiminer.demos.wallet.api.TransferTokensChannel

class SendModel(client: ApiClient) : ViewModel() {
    val tokens by lazy { TransferTokensChannel(client) }

    override fun onCleared() {
        tokens.cancel()
    }
}
