package com.apiminer.demos.wallet

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.apiminer.demos.wallet.api.Auth
import org.jetbrains.anko.startActivity
import org.koin.android.ext.android.inject

/**
 * The launch Activity simply redirects the user to the appropriate screen.
 */
class MainActivity : AppCompatActivity() {
    val auth : Auth by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (auth.isLoggedIn()) {
            startActivity<WalletActivity>()
        } else {
            startActivity<LoginActivity>()
        }
        finish()
    }
}
