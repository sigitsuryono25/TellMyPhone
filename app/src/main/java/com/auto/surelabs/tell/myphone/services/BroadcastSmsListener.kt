package com.auto.surelabs.tell.myphone.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.media.AudioManager
import android.media.MediaPlayer
import android.telephony.SmsManager
import android.telephony.SmsMessage
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.auto.surelabs.tell.myphone.MainActivity
import com.auto.surelabs.tell.myphone.R
import com.google.android.gms.location.*

@Suppress("DEPRECATION")
class BroadcastSmsListener : BroadcastReceiver() {
    private var context: Context? = null
    private var mediaPlayer: MediaPlayer? = null

    private var mFusedLocationProviderClient: FusedLocationProviderClient? = null
    private lateinit var mLocationCallback: LocationCallback
    private var mIsReceivingUpdates = false
    private lateinit var callback: UserLocationCallback

    override fun onReceive(context: Context?, intent: Intent?) {
        this.context = context
        //prepare telpon
        val prefs = context?.getSharedPreferences(
            MainActivity::class.java.`package`.toString(),
            Context.MODE_PRIVATE
        )
        val tujuan = prefs?.getString("no_tel", "")
        callback = UserLocationCallback()
        if (intent?.action?.equals("android.provider.Telephony.SMS_RECEIVED") == true) {
            val bundler = intent.extras
            val msgs: Array<SmsMessage?>
//            var msgFrom: String?
            if (bundler?.isEmpty == false) {
                try {
                    val pdus = bundler.get("pdus") as Array<*>
                    msgs = arrayOfNulls(pdus.size)
                    for (i in msgs.indices) {
                        msgs[i] =
                            SmsMessage.createFromPdu(pdus[i] as ByteArray)
//                        msgFrom = msgs[i]?.originatingAddress
                        val msgBody = msgs[i]?.messageBody

                        doAction(msgBody, tujuan)
                    }
                } catch (e: Exception) {
                    Log.d("Exception caught", e.message.toString())
                }
            }
        }
    }

    private fun doAction(params: String?, tujuan: String?) {
        if (params.equals("req ringing", true)) {
            val am = context?.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            am.setStreamVolume(AudioManager.STREAM_MUSIC, 100, 0)
            if (mediaPlayer != null) {
                mediaPlayer?.release()
                mediaPlayer?.reset()
            }
            mediaPlayer = MediaPlayer.create(context, R.raw.android_music)
            mediaPlayer?.setVolume(1f, 1f)
            mediaPlayer?.start()

            Log.d("REQ", "RINGING")

        } else if (params.equals("req loc", true)) {
            getCurrentLocationUpdates(context, callback, tujuan)
        } else if (params.equals("req stop ringing", true)) {
            if (mediaPlayer != null) {
                mediaPlayer?.stop()
                mediaPlayer?.release()
            }
        } else if (params.equals("req stop loc", true)) {
            getCurrentLocationUpdates(context, callback, tujuan)
        }
    }

    private fun getCurrentLocationUpdates(
        context: Context?,
        callback: UserLocationCallback,
        tujuan: String?
    ) {
        if (mIsReceivingUpdates) {
            callback.onFailedRequest("Device is already receiving updates")
            return
        }

        // Set up the LocationCallback for the request
        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                callback.onLocationResult(locationResult?.lastLocation, tujuan)
            }
        }

        // Start the request
        val mLocationRequest = LocationRequest()
        mLocationRequest.apply {
            interval = 1000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context!!)
        val checkPermission = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        )
        if (checkPermission == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationProviderClient?.requestLocationUpdates(
                mLocationRequest,
                mLocationCallback,
                null
            )
        }
        // Update the request state flag
        mIsReceivingUpdates = true
    }

    private fun stopLocationUpdates() {
        mFusedLocationProviderClient?.removeLocationUpdates(mLocationCallback)
        mIsReceivingUpdates = false
        Log.i("Remove Location", "Location updates removed")
    }

    inner class UserLocationCallback {
        fun onLocationResult(location: Location?, tujuan: String?) {
            val url =
                "http://www.google.com/maps/place/${location?.latitude},${location?.longitude}"
            val mSmsManager = SmsManager.getDefault()
            mSmsManager.sendTextMessage(tujuan, null, "Here's the phone location $url", null, null)
            Log.d("REQ", "$tujuan")

            stopLocationUpdates()
        }

        fun onFailedRequest(message: String?) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }

    }
}