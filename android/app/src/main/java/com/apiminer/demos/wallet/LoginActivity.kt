package com.apiminer.demos.wallet

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import com.apiminer.demos.wallet.api.Auth
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.*
import org.koin.android.ext.android.inject

/**
 * A login screen that offers login via email/password.
 */
class LoginActivity : AppCompatActivity() {

    val auth: Auth by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        setTitle(R.string.login)
        password.setOnEditorActionListener(TextView.OnEditorActionListener { _, id, _ ->
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptLogin()
                return@OnEditorActionListener true
            }
            false
        })

        sign_in.setOnClickListener { attemptLogin() }
        register.setOnClickListener { startActivity<RegisterActivity>() }

        if (auth.getEmail().isNotEmpty()) {
            email.setText(auth.getEmail())
            password.requestFocus()
        }
    }

    fun attemptLogin() {
        val emailInput = email.text.toString()
        val passwordInput = password.text.toString()

        if (emailInput.isEmpty()) {
            alert(R.string.please_enter_email).show()
            return
        }

        if (passwordInput.isEmpty()) {
            alert(R.string.please_enter_password).show()
            return
        }

        login_progress.visibility = View.VISIBLE
        sign_in.isEnabled = false

        // Launch a coroutine for the background work
        launch(UI) {
            val result = auth.login(emailInput, passwordInput).await()

            result.fold({ _ ->
                startActivity(intentFor<WalletActivity>().clearTask().newTask())
            }, { error ->
                val message = error.message
                if (message != null) {
                    alert(message).show()
                } else {
                    alert(R.string.unknown_error).show()
                }
            })

            login_progress.visibility = View.INVISIBLE
            sign_in.isEnabled = true
        }
    }
}
