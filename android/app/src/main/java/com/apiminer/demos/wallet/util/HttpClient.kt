package com.apiminer.demos.wallet.util

import com.facebook.stetho.urlconnection.ByteArrayRequestEntity
import com.facebook.stetho.urlconnection.StethoURLConnectionManager
import com.github.kittinunf.fuel.core.*
import com.github.kittinunf.fuel.util.TestConfiguration
import java.io.BufferedOutputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.Proxy
import java.net.URLConnection
import java.util.zip.GZIPInputStream
import javax.net.ssl.HttpsURLConnection

/**
 * Customized HttpClient for Fuel that integrates Stetho.
 */
internal class HttpClient(private val proxy: Proxy? = null) : Client {
    val testConfiguration = TestConfiguration(timeout = null, blocking = false)

    override fun executeRequest(request: Request): Response {
        val stetho = StethoURLConnectionManager("StethoFuelConnectionManager")
        try {
            val connection = establishConnection(request) as HttpURLConnection
            connection.apply {
                connectTimeout = testConfiguration.coerceTimeout(request.timeoutInMillisecond)
                readTimeout = testConfiguration.coerceTimeoutRead(request.timeoutReadInMillisecond)
                doInput = true
                useCaches = false
                requestMethod = if (request.method == Method.PATCH) Method.POST.value else request.method.value
                instanceFollowRedirects = false

                for ((key, value) in request.headers) {
                    setRequestProperty(key, value)
                }

                if (request.method == Method.PATCH) {
                    setRequestProperty("X-HTTP-Method-Override", "PATCH")
                }

                setDoOutput(connection, request.method)

                val stream = ByteArrayOutputStream()
                request.bodyCallback?.let { it(request, stream, 0) }
                stetho.preConnect(connection, ByteArrayRequestEntity(stream.toByteArray()))

                setBodyIfDoOutput(connection, request)
            }

            val contentEncoding = connection.contentEncoding ?: ""

            return Response(
                    url = request.url,
                    headers = connection.headerFields.filterKeys { it != null },
                    contentLength = connection.contentLength.toLong(),
                    statusCode = connection.responseCode,
                    responseMessage = connection.responseMessage.orEmpty(),
                    dataStream = try {
                        val stream = connection.errorStream ?: connection.inputStream
                        stetho.interpretResponseStream(stream)
                        if (contentEncoding.compareTo("gzip", true) == 0) GZIPInputStream(stream) else stream
                    } catch (exception: IOException) {
                        try {
                            connection.errorStream ?: connection.inputStream?.close()
                        } catch (exception: IOException) {
                        }
                        ByteArrayInputStream(ByteArray(0))
                    } finally {
                        stetho.postConnect()
                    }
            )
        } catch (exception: Exception) {
            throw FuelError(exception, ByteArray(0), Response(request.url))
        } finally {
            //As per Android documentation, a connection that is not explicitly disconnected
            //will be pooled and reused!  So, don't close it as we need inputStream later!
            //connection.disconnect()
        }
    }

    private fun establishConnection(request: Request): URLConnection {
        val urlConnection = if (proxy != null) request.url.openConnection(proxy) else request.url.openConnection()
        return if (request.url.protocol == "https") {
            urlConnection as HttpsURLConnection
        } else {
            urlConnection as HttpURLConnection
        }
    }

    private fun setBodyIfDoOutput(connection: HttpURLConnection, request: Request) {
        val bodyCallback = request.bodyCallback
        if (bodyCallback != null && connection.doOutput) {
            val contentLength = bodyCallback(request, null, 0)

            if (request.type == Request.Type.UPLOAD)
                connection.setFixedLengthStreamingMode(contentLength.toInt())

            BufferedOutputStream(connection.outputStream).use {
                bodyCallback(request, it, contentLength)
            }
        }
    }

    private fun setDoOutput(connection: HttpURLConnection, method: Method) = when (method) {
        Method.GET, Method.DELETE, Method.HEAD -> connection.doOutput = false
        Method.POST, Method.PUT, Method.PATCH -> connection.doOutput = true
    }
}


