package com.apiminer.demos.wallet.data

import com.apiminer.demos.wallet.api.Transfer
import com.chibatching.kotpref.KotprefModel
import com.chibatching.kotpref.gsonpref.gsonPref

/**
 * Very simple data store using android SharedPreferences.
 */
object DataStore : KotprefModel() {
    var accessToken by stringPref()
    var tokenExpiration by longPref()
    var email by stringPref()
    var accountAddress by stringPref()
    var balance by stringPref()
    var transfers by gsonPref(arrayOf<Transfer>())
    var pendingTransfer by stringPref()
}
