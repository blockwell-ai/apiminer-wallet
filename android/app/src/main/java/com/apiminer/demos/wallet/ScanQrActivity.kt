package com.apiminer.demos.wallet

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import com.google.zxing.BarcodeFormat
import kotlinx.android.synthetic.main.activity_scan_qr.*
import org.jetbrains.anko.longToast

/**
 * Activity that scans a QR code, and finishes with a result containing
 * the Ethereum address scanned.
 *
 * Also asks for dynamic permissions for the Camera.
 */
class ScanQrActivity : AppCompatActivity() {
    companion object {
        /**
         * Request code for using the scanner.
         */
        const val REQUEST_CODE = 101

        /**
         * Request code for the camera permission.
         */
        const val REQUEST_CAMERA = 102

        /**
         * Basic regex for an Ethereum address.
         */
        val ETH_REGEX = Regex("^0x[a-fA-F0-9]{40}$")
    }

    var codeScanner: CodeScanner? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_qr)
        setSupportActionBar(toolbar)

        // Go through the permission request flow, eventually calling prepareScanner if successful
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted, do we need to show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                Snackbar.make(main_layout, R.string.permission_camera_rationale,
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.ok) {
                            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA)
                        }
                        .show()
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA)
            }
        } else {
            prepareScanner()
        }

        toolbar.setNavigationOnClickListener { finish() }
    }

    override fun onDestroy() {
        super.onDestroy()
        codeScanner?.releaseResources()
    }

    fun prepareScanner() {
        val scanner = CodeScanner(this, scanner_view)
        codeScanner = scanner

        scanner.formats = listOf(BarcodeFormat.QR_CODE)
        scanner.scanMode = ScanMode.SINGLE

        scanner.decodeCallback = DecodeCallback {
            runOnUiThread {
                // Only accept the scan if it's an Ethereum address
                if (ETH_REGEX.matches(it.text)) {
                    val resultIntent = Intent()
                    resultIntent.putExtra("address", it.text)
                    setResult(Activity.RESULT_OK, resultIntent)
                    finish()
                } else {
                    longToast(R.string.qr_not_address)
                }
            }
        }
        scanner.errorCallback = ErrorCallback {
            runOnUiThread {
                longToast(getString(R.string.camera_error, it.message))
            }
        }
        scanner.startPreview()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CAMERA) {
            if (grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
                // Camera permission has been granted, preview can be displayed
                Log.i("Camera", "CAMERA permission has now been granted. Showing preview.")
                prepareScanner()
            } else {
                Log.i("Camera", "CAMERA permission was NOT granted.")
                finish()
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }
}
