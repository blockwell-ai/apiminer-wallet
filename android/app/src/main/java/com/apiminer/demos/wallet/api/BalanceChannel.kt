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
 * Get wallet balance updates.
 *
 * Uses coroutines and channels for background work.
 *
 * Note that this needs to have [cancel] called in order for the updates to stop.
 */
class BalanceChannel(val client: ApiClient) : CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default

    /**
     * Channel for receiving the balance.
     *
     * Conflated broadcast channels only send the latest result to new subscribers,
     * and then every update while they're subscribed.
     */
    val channel: BroadcastChannel<BalanceResponse> = ConflatedBroadcastChannel()

    /**
     * Job reference lets us cancel the refresh loop.
     */
    val job: Job

    init {
        job = launch {
            // If we have a cached balance, offer that first
            if (DataStore.balance.isNotEmpty()) {
                if (!channel.isClosedForSend) {
                    channel.offer(BalanceResponse(DataStore.balance, DataStore.accountAddress))
                }
            }

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
            refreshBalance()
            delay(5000)
        }
    }

    /**
     * Refreshes the user's wallet balance.
     */
    suspend fun refreshBalance() {
        val response = client.getWithAuth("tokens/balance", DataStore.accessToken, BalanceResponse.Deserializer)

        // Cache the results and offer the new balance to the channel
        response.success { result ->
            try {
                DataStore.accountAddress = result.account
                DataStore.balance = result.balance
            } catch (e: Exception) {
                Log.e("BalanceChannel", "DataStore exception", e)
            }
            if (!channel.isClosedForSend) {
                channel.offer(result)
            }
        }
    }

    /**
     * Cancels both the channel and any pending refresh jobs.
     */
    fun cancel() {
        channel.cancel()
        job.cancel()
    }
}

data class BalanceResponse(
        val balance: String,
        val account: String
) {
    object Deserializer : ResponseDeserializable<BalanceResponse> {
        override fun deserialize(content: String) = Gson().fromJson(content, BalanceResponse::class.java)
    }
}
