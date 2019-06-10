package com.apiminer.demos.wallet

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import com.apiminer.demos.wallet.api.Auth
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.*
import org.koin.android.ext.android.inject

/**
 * A simple registration screen with email and password fields.
 */
class RegisterActivity : AppCompatActivity() {

    val auth: Auth by inject()
    val scope = MainScope()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        setTitle(R.string.register)
        password.setOnEditorActionListener(TextView.OnEditorActionListener { _, id, _ ->
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptRegister()
                return@OnEditorActionListener true
            }
            false
        })

        register.setOnClickListener { attemptRegister() }
        login.setOnClickListener {
            startActivity<LoginActivity>()
        }

        if (auth.getEmail().isNotEmpty()) {
            email.setText(auth.getEmail())
            password.requestFocus()
        }
    }

    fun attemptRegister() {
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

        register_progress.visibility = View.VISIBLE
        register.isEnabled = false

        // Launch a coroutine for the background work
        scope.launch {
            val result = auth.register(emailInput, passwordInput)

            result.fold({
                startActivity(intentFor<WalletActivity>().clearTask().newTask())
            }, { error ->
                val message = error.message
                if (message != null) {
                    alert(message).show()
                } else {
                    alert(R.string.unknown_error).show()
                }
            })

            register_progress.visibility = View.INVISIBLE
            register.isEnabled = true
        }
    }
}
