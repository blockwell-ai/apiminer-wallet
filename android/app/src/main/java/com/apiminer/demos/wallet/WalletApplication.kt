package com.apiminer.demos.wallet

import android.app.Application
import com.chibatching.kotpref.Kotpref
import com.chibatching.kotpref.gsonpref.gson
import com.facebook.stetho.Stetho
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.stetho.StethoHook
import com.google.gson.Gson
import org.koin.android.ext.android.get
import org.koin.android.ext.android.startKoin

@Suppress("unused")
class WalletApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        Stetho.initializeWithDefaults(this)
        Kotpref.init(this)

        // Fuel defaults
        FuelManager.instance.basePath = BuildConfig.API_BASEURL
        FuelManager.instance.baseHeaders = mapOf(Pair("Content-Type", "application/json"))
        FuelManager.instance.hook = StethoHook()

        // Dependency injection
        startKoin(listOf(mainModule))

        // Need to inject this directly to Kotpref
        Kotpref.gson = get<Gson>()
    }
}
