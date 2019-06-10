package com.apiminer.demos.wallet.api

import android.util.Log
import com.apiminer.demos.wallet.data.DataStore
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.success
import com.google.gson.Gson
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlin.coroutines.CoroutineContext

/**
 * Transfer tokens and subscribe to results.
 *
 * Uses coroutine channels for responses.
 */
class TransferTokensChannel(val client: ApiClient) : CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default

    /**
     * Channel for receiving the token transfer response.
     *
     * Conflated broadcast channels only send the latest result to new subscribers,
     * and then every update while they're subscribed.
     */
    val channel: BroadcastChannel<Result<TransferResponse, Exception>> = ConflatedBroadcastChannel()

    /**
     * Keep track of the job so we can make sure we don't double submit.
     */
    var job: Job? = null

    /**
     * Transfer tokens.
     *
     * @param to Token recipient, can be an Ethereum address or another user's email address
     * @param value Amount to send, in the smallest unit (eg. wei)
     */
    fun transfer(to: String, value: String) {
        if (isActive()) {
            Log.w("TransferTokensChannel", "Job already active, not making transfer")
            return
        }

        job = launch {
            val response = client.postWithAuth("tokens/transfers",
                    DataStore.accessToken,
                    TransferResponse.Deserializer,
                    TransferRequest(to, value))

            response.success { result ->
                try {
                    DataStore.pendingTransfer = result.transactionId
                } catch (e: Exception) {
                    Log.e("TransferTokensChannel", "DataStore exception", e)
                }
            }
            if (!channel.isClosedForSend) {
                channel.offer(response)
            }
        }
    }

    fun isActive(): Boolean {
        val currentJob = job
        return currentJob != null && currentJob.isActive
    }

    /**
     * Cancels the channel for receiving the request status.
     *
     * Note that this does not cancel the actual job, because we want to avoid an
     * inconsistent state with an important POST call like this.
     */
    fun cancel() {
        channel.cancel()
    }
}

data class TransferRequest(
        val to: String,
        val value: String
)

data class TransferResponse(
        val transactionId: String
) {
    object Deserializer : ResponseDeserializable<TransferResponse> {
        override fun deserialize(content: String) = Gson().fromJson(content, TransferResponse::class.java)
    }
}
