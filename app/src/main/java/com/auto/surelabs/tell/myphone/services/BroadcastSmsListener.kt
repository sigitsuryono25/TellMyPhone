package com.auto.surelabs.tell.myphone.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.telephony.SmsManager
import android.telephony.SmsMessage
import android.util.Log
import androidx.core.content.ContextCompat
import com.auto.surelabs.tell.myphone.MainActivity
import com.auto.surelabs.tell.myphone.R
import com.google.android.gms.location.*

@Suppress("DEPRECATION")
class BroadcastSmsListener : BroadcastReceiver() {
    private var context: Context? = null
    private var mediaPlayer: MediaPlayer? = null
    private var mFusedLocationProviderClient: FusedLocationProviderClient? = null

    override fun onReceive(context: Context?, intent: Intent?) {
        this.context = context
        //prepare telpon
        val prefs = context?.getSharedPreferences(
            MainActivity::class.java.`package`.toString(),
            Context.MODE_PRIVATE
        )
        val tujuan = prefs?.getString("no_tel", "")
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

    private fun doAction(params: String?, msgFrom: String?) {
        if (params.equals("req ringing", true)) {
            val am = context?.getSystemService(Context.AUDIO_SERVICE) as AudioManager
//                    am.ringerMode = AudioManager.RINGER_MODE_NORMAL
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
            updateLocation(context, msgFrom)
        } else if (params.equals("req stop ringing", true)) {
            if (mediaPlayer != null) {
                mediaPlayer?.stop()
                mediaPlayer?.release()
            }
        } else if (params.equals("req stop loc", true)) {
            updateLocation(context, msgFrom, true)
        }
    }

    private fun updateLocation(context: Context?, msgFrom: String?, isCancel: Boolean = false) {
        val loc = LocationRequest()
        loc.apply {
            interval = 1000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context!!)
        val permissionCheckedInputStream = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        )
        if (permissionCheckedInputStream == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationProviderClient?.requestLocationUpdates(
                loc,
                MyLocationCallback(msgFrom),
                null
            )
        }
        mFusedLocationProviderClient?.removeLocationUpdates(MyLocationCallback(msgFrom))
    }

}

class MyLocationCallback(private val msgFrom: String?) : LocationCallback() {
    override fun onLocationResult(p0: LocationResult?) {
        super.onLocationResult(p0)
        val lat = p0?.lastLocation?.latitude
        val lon = p0?.lastLocation?.longitude
        val url = "http://www.google.com/maps/place/$lat,$lon"
        val mSmsManager = SmsManager.getDefault()
        mSmsManager.sendTextMessage(msgFrom, null, "Here's the phone location $url", null, null)
        Log.d("REQ", "$msgFrom")
    }
}
