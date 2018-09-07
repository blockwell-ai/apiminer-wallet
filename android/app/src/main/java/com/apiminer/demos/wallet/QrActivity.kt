package com.apiminer.demos.wallet

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import com.apiminer.demos.wallet.data.DataStore
import kotlinx.android.synthetic.main.activity_qr.*
import net.glxn.qrgen.android.QRCode
import org.jetbrains.anko.displayMetrics
import kotlin.math.roundToInt

/**
 * Shows a QR code of the user's Ethereum address.
 */
class QrActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr)
        account_address.text = DataStore.accountAddress

        val size = (displayMetrics.widthPixels * 0.75).roundToInt()
        val bitmap = QRCode.from(DataStore.accountAddress)
                .withColor(ContextCompat.getColor(this, R.color.colorPrimaryDark), ContextCompat.getColor(this, R.color.background_light))
                .withSize(size, size)
                .bitmap()
        qr_image.setImageBitmap(bitmap)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
