package com.apiminer.demos.wallet.api

import android.util.Log
import com.apiminer.demos.wallet.data.DataStore
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.github.kittinunf.result.success
import com.google.gson.Gson
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlin.coroutines.CoroutineContext

/**
 * Listen to the status of a transfer.
 *
 * Note that this needs to have [cancel] called in order for the updates to stop.
 */
class TransferStatusChannel(val client: ApiClient) : CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default

    /**
     * Channel for receiving new transfer statuses.
     *
     * Conflated broadcast channels only send the latest result to new subscribers,
     * and then every update while they're subscribed.
     */
    val channel: BroadcastChannel<TransferStatusResponse> = ConflatedBroadcastChannel()

    /**
     * Job reference lets us cancel the refresh loop.
     */
    val job: Job

    init {
        job = launch {
            // This blocks the coroutine indefinitely until it's broken
            refreshLoop()
        }
    }

    private suspend fun refreshLoop() {
        while (true) {
            // Stop the loop if the channel is closed permanently
            if (channel.isClosedForSend) {
                break
            }
            refreshStatus()
            delay(5000)
        }
    }

    /**
     * Refreshes the status of a transfer from the backend.
     */
    suspend fun refreshStatus() {
        val response = client.getWithAuth("tokens/transactions/${DataStore.pendingTransfer}",
                DataStore.accessToken,
                TransferStatusResponse.Deserializer)

        response.success { result ->
            // We only care about a completed or error status
            if (result.status == "completed" || result.status == "error") {
                try {
                    // Clear the pending transaction in the cache
                    DataStore.pendingTransfer = ""
                } catch (e: Exception) {
                    Log.e("TransferStatusChannel", "DataStore exception", e)
                }

                if (!channel.isClosedForSend) {
                    channel.offer(result)
                    // If the channel isn't already canceled, do that now
                    cancel()
                }
            }
        }
    }

    /**
     * Cancels both the channel and any pending requests.
     */
    fun cancel() {
        channel.cancel()
        job.cancel()
    }
}

data class TransferStatusResponse(
        val id: String,
        val status: String,
        val error: String?
) {
    object Deserializer : ResponseDeserializable<TransferStatusResponse> {
        override fun deserialize(content: String) = Gson().fromJson(content, TransferStatusResponse::class.java)
    }
}
