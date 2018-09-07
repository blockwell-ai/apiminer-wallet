package com.apiminer.demos.wallet

import com.apiminer.demos.wallet.api.ApiClient
import com.apiminer.demos.wallet.api.Auth
import com.apiminer.demos.wallet.viewmodel.SendModel
import com.apiminer.demos.wallet.viewmodel.WalletModel
import com.google.gson.Gson
import org.koin.android.architecture.ext.viewModel
import org.koin.dsl.module.applicationContext

/**
 * Koin dependency injection module.
 */
val mainModule = applicationContext {
    bean { Gson() }
    bean { ApiClient(get()) }
    bean { Auth(get()) }
    viewModel { WalletModel(get()) }
    viewModel { SendModel(get()) }
}
