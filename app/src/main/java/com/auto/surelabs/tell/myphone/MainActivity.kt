package com.auto.surelabs.tell.myphone

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        version.text = BuildConfig.VERSION_NAME


        sms.setOnClickListener {
            requestPermissions(arrayOf(android.Manifest.permission.RECEIVE_SMS), 0)
        }

        readPhoneState.setOnClickListener {
            requestPermissions(arrayOf(android.Manifest.permission.READ_PHONE_STATE), 1)
        }

        location.setOnClickListener {
            requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 2)
        }

        val smsPermission = checkSelfPermission(android.Manifest.permission.RECEIVE_SMS)
        val readPhoneStatePermission =
            checkSelfPermission(android.Manifest.permission.READ_PHONE_STATE)
        val locationPermission =
            checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)

        if (smsPermission == PackageManager.PERMISSION_GRANTED) {
            sms.text = "Terima/Mengirimkan SMS: Permission Granted"
            sms.setBackgroundColor(getColor(R.color.colorAccent))
            sms.isEnabled = false
        }

        if (readPhoneStatePermission == PackageManager.PERMISSION_GRANTED) {
            readPhoneState.text = "Membaca Status Perangkat: Permission Granted"
            readPhoneState.setBackgroundColor(getColor(R.color.colorAccent))
            readPhoneState.isEnabled = false
        }

        if (locationPermission == PackageManager.PERMISSION_GRANTED) {
            location.text = "Lokasi: Permission Granted"
            location.setBackgroundColor(getColor(R.color.colorAccent))
            location.isEnabled = false
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 0) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                sms.text = "Terima/Mengirimkan SMS: Permission Granted"
                sms.setBackgroundColor(getColor(R.color.colorAccent))
                sms.isEnabled = false
            }
        } else if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                readPhoneState.text = "Membaca Status Perangkat: Permission Granted"
                readPhoneState.setBackgroundColor(getColor(R.color.colorAccent))
                readPhoneState.isEnabled = false
            }
        } else if (requestCode == 2) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                location.text = "Lokasi: Permission Granted"
                location.setBackgroundColor(getColor(R.color.colorAccent))
                location.isEnabled = false
            }
        }
    }
}
