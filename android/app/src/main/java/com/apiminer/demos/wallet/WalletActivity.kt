package com.apiminer.demos.wallet

import android.content.ClipData
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.apiminer.demos.wallet.api.Auth
import com.apiminer.demos.wallet.view.TransferAdapter
import com.apiminer.demos.wallet.viewmodel.WalletModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_wallet.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import org.jetbrains.anko.*
import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.android.inject
import java.math.BigDecimal

/**
 * The main wallet activity.
 */
class WalletActivity : AppCompatActivity() {

    val auth: Auth by inject()
    val scope = MainScope()
    val model by viewModel<WalletModel>()

    // The parent job for all background work this activity subscribes to
    var job: Job? = null

    lateinit var adapter: TransferAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wallet)
        setSupportActionBar(toolbar)
        title = ""

        adapter = TransferAdapter()
        recycler.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        recycler.adapter = adapter

        val decorator = androidx.recyclerview.widget.DividerItemDecoration(this, androidx.recyclerview.widget.LinearLayoutManager.VERTICAL)
        decorator.setDrawable(getDrawable(R.drawable.divider))
        recycler.addItemDecoration(decorator)

        fab.setOnClickListener {
            startActivity<SendActivity>()
        }
        account_address.setOnClickListener {
            val clipData = ClipData.newPlainText("Account address", account_address.text)
            clipboardManager.primaryClip = clipData
            longToast(R.string.account_copied)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_wallet, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_refresh -> {
                toast(R.string.refreshing_balance)
                scope.launch { model.balance.refreshBalance() }
                true
            }
            R.id.action_qr -> {
                startActivity<QrActivity>()
                true
            }
            R.id.action_licenses -> {
                startActivity<LicensesActivity>()
                true
            }
            else -> false
        }
    }

    override fun onResume() {
        super.onResume()

        if (!auth.isLoggedIn()) {
            startActivity(intentFor<MainActivity>().clearTask().newTask())
        } else {
            subscribeToUpdates()
        }
    }

    override fun onPause() {
        super.onPause()
        job?.cancel()
        job = null
    }

    fun setBalance(newBalance: String): Boolean {
        val dec = BigDecimal(newBalance).divide(BigDecimal(1e18))
        val oldText = balance.text
        balance.text = dec.toPlainString()
        return oldText.isNotEmpty() && oldText != balance.text
    }

    /**
     * Convenience method for subscribing this activity to background updates.
     */
    fun subscribeToUpdates() {
        val newJob = scope.launch {
            launch { subscribeToBalance() }
            launch { subscribeToTransfersHistory() }
            launch { subscribeToTransferStatus() }
        }
        job = newJob
    }

    /**
     * Subscribe to wallet balance updates.
     */
    suspend fun subscribeToBalance() {
        model.balance.channel.consumeEach {
            if (setBalance(it.balance)) {
                // Update transfers list if balance changed
                model.transfers.refresh()
            }
            account_address.text = it.account
            adapter.userAccount = it.account
        }
    }

    /**
     * Subscribe to transfers history data updates.
     */
    suspend fun subscribeToTransfersHistory() {
        model.transfers.channel.consumeEach {
            adapter.setTransfers(it)
        }
    }

    /**
     * Subscribe to status updates for a pending token transfer.
     */
    suspend fun subscribeToTransferStatus() {
        if (model.getPendingTransfer().isNotEmpty()) {
            val snackbar = Snackbar.make(main_layout, R.string.pending_transfer, Snackbar.LENGTH_INDEFINITE)
            snackbar.show()

            model.transferStatus.channel.consumeEach {
                if (it.status == "completed") {
                    snackbar.dismiss()
                } else if (it.status == "error") {
                    alert(getString(R.string.transfer_failed) + it.error).show()
                    snackbar.dismiss()
                }
            }
        }
    }
}
