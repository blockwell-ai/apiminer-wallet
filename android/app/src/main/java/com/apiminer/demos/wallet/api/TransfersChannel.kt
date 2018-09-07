package com.apiminer.demos.wallet.api

import android.util.Log
import com.apiminer.demos.wallet.data.DataStore
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.github.kittinunf.result.success
import com.google.gson.Gson
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.BroadcastChannel
import kotlinx.coroutines.experimental.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.experimental.launch

/**
 * Get transfer history updates.
 *
 * Uses coroutines and channels for background work.
 *
 * Note that this needs to have [cancel] called in order for the updates to stop.
 */
class TransfersChannel(val client: ApiClient) {
    /**
     * Channel for receiving the transfer history.
     *
     * Conflated broadcast channels only send the latest result to new subscribers,
     * and then every update while they're subscribed.
     */
    val channel: BroadcastChannel<Array<Transfer>> = ConflatedBroadcastChannel()

    init {
        launch(CommonPool) {
            // If we have a cached history, offer that first
            if (DataStore.transfers.isNotEmpty()) {
                if (!channel.isClosedForSend) {
                    channel.offer(DataStore.transfers)
                }
            }

            // Refresh the history once
            refresh()
        }
    }

    /**
     * Fetches the transfer history from the backend.
     */
    suspend fun refresh() = async(CommonPool) {
        val response = client.getWithAuth("tokens/transfers", DataStore.accessToken, Transfer.Deserializer)

        response.success { result ->
            try {
                DataStore.transfers = result
            } catch (e: Exception) {
                Log.e("TransfersChannel", "DataStore exception", e)
            }
            if (!channel.isClosedForSend) {
                channel.offer(result)
            }
        }

        response
    }

    /**
     * Cancels the channel, freeing resources.
     */
    fun cancel() {
        channel.cancel()
    }
}

data class Transfer(
        val from: String,
        val to: String,
        val value: String,
        val transactionHash: String,
        val blockNumber: Int
) {
    object Deserializer : ResponseDeserializable<Array<Transfer>> {
        override fun deserialize(content: String) = Gson().fromJson(content, Array<Transfer>::class.java)
    }
}
