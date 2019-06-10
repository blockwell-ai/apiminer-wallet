package com.apiminer.demos.wallet

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_licenses.*

/**
 * Activity to display open source licenses.
 */
class LicensesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_licenses)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        webview.loadUrl("file:///android_asset/licenses.html")
    }

    override fun onNavigateUp(): Boolean {
        finish()
        return true
    }
}
