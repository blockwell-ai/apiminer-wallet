package com.apiminer.demos.wallet

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.apiminer.demos.wallet.api.TransferResponse
import com.apiminer.demos.wallet.data.DataStore
import com.apiminer.demos.wallet.viewmodel.SendModel
import com.github.kittinunf.result.Result
import kotlinx.android.synthetic.main.activity_send.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.alert
import org.jetbrains.anko.startActivityForResult
import org.koin.android.architecture.ext.viewModel
import java.math.BigDecimal

/**
 * Sending tokens to another user or an Ethereum address.
 */
class SendActivity : AppCompatActivity() {

    val model by viewModel<SendModel>()
    var channel: ReceiveChannel<Result<TransferResponse, Exception>>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send)
        setTitle(R.string.send_wallet_coins)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        send.setOnClickListener { attemptSend() }
    }

    override fun onResume() {
        super.onResume()
        listenToResult()

        // Check to see if there's already an active submission, for example after screen rotation
        if (model.tokens.isActive()) {
            send_progress.visibility = View.VISIBLE
            send.isEnabled = false
        }
    }

    override fun onPause() {
        super.onPause()
        channel?.apply {
            // This cancels the subscription to the events, but not the actual transfer request.
            // A new version of this activity can subscribe again, like after a screen rotation.
            cancel()
        }
    }

    fun attemptSend() = launch(UI) {
        val to = recipient.text.toString()

        // Convert the user entered value to the smallest unit
        val value = BigDecimal(value.text.toString()).multiply(BigDecimal(1e18))

        if (value > BigDecimal(DataStore.balance)) {
            alert(R.string.not_enough_balance).show()
            return@launch
        }

        send_progress.visibility = View.VISIBLE
        send.isEnabled = false

        // This is inside a coroutine, but we're not awaiting for results, so the
        // attemptSend method ends right away. See below in listenToResult for actually
        // receiving the result.
        model.tokens.transfer(to, value.toBigInteger().toString())
    }

    fun listenToResult() = launch(UI) {
        // We're creating a new subscription to the token transfer event channel
        val subscription = model.tokens.channel.openSubscription()
        channel = subscription

        subscription.consumeEach {
            it.fold({ _ ->
                finish()
            }, { error ->
                send_progress.visibility = View.INVISIBLE
                send.isEnabled = true
                val message = error.message
                if (message != null) {
                    alert(message).show()
                } else {
                    alert(R.string.unknown_error).show()
                }
            })
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_send, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_qr -> {
                startActivityForResult<ScanQrActivity>(ScanQrActivity.REQUEST_CODE)
                true
            }
            else -> false
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Receiving an address from a QR code
        if (requestCode == ScanQrActivity.REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            recipient.setText(data.getStringExtra("address"))
        }
    }
}
