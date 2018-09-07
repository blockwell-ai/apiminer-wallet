package com.apiminer.demos.wallet.api

import com.apiminer.demos.wallet.data.DataStore
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.github.kittinunf.result.success
import com.google.gson.Gson
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async

/**
 * Logic for login and registration
 */
class Auth(val client: ApiClient) {

    fun isLoggedIn(): Boolean {
        return DataStore.accessToken.isNotEmpty()
                && DataStore.tokenExpiration > 0L
                && DataStore.tokenExpiration * 1000 > System.currentTimeMillis()
    }

    fun getEmail(): String {
        return DataStore.email
    }

    suspend fun register(email: String, password: String) = async(CommonPool) {
        val response = client.post("auth/register", AuthResponse.Deserializer, AuthRequest(email, password))

        response.success {result ->
            DataStore.email = email
            DataStore.accessToken = result.token
            DataStore.tokenExpiration = result.expiration
        }

        response
    }

    suspend fun login(email: String, password: String) = async(CommonPool) {
        val response = client.post("auth/login", AuthResponse.Deserializer, AuthRequest(email, password))

        response.success {result ->
            DataStore.email = email
            DataStore.accessToken = result.token
            DataStore.tokenExpiration = result.expiration
        }

        response
    }
}

data class AuthRequest(val email: String, val password: String)

data class AuthResponse(
        val token: String = "",
        val expiration: Long = 0L
) {
    object Deserializer : ResponseDeserializable<AuthResponse> {
        override fun deserialize(content: String) = Gson().fromJson(content, AuthResponse::class.java)
    }
}
