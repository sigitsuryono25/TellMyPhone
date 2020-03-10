package com.auto.surelabs.tell.myphone.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.telephony.SmsManager
import android.telephony.SmsMessage
import android.util.Log
import com.auto.surelabs.tell.myphone.R

@Suppress("DEPRECATION")
class BroadcastSmsListener : BroadcastReceiver() {
    private var context: Context? = null
    private var mediaPlayer: MediaPlayer? = null
    override fun onReceive(context: Context?, intent: Intent?) {
        this.context = context
        if (intent?.action?.equals("android.provider.Telephony.SMS_RECEIVED") == true) {
            val bundler = intent.extras
            val msgs: Array<SmsMessage?>
            var msgFrom: String?
            if (bundler?.isEmpty == false) {
                try {
                    val pdus = bundler.get("pdus") as Array<*>
                    msgs = arrayOfNulls(pdus.size)
                    for (i in msgs.indices) {
                        msgs[i] =
                            SmsMessage.createFromPdu(pdus[i] as ByteArray)
                        msgFrom = msgs[i]?.originatingAddress
                        val msgBody = msgs[i]?.messageBody

                        doAction(msgBody, msgFrom)
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
            val mSmsManager = SmsManager.getDefault()
            mSmsManager.sendTextMessage(msgFrom, null, "location", null, null)
            Log.d("REQ", "req loc")
        }
    }
}