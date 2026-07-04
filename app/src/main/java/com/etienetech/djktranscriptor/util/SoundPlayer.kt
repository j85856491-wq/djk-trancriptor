package com.etienetech.djktranscriptor.util

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri

/**
 * Plays sound feedback for voice commands.
 *
 * DJK Transcriptor - Developed by Etienne Tech
 */
class SoundPlayer(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null

    /**
     * Play a notification-style sound for feedback.
     */
    fun playSuccessSound() {
        try {
            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            playUri(uri)
        } catch (e: Exception) {
            // Silently fail - sound is non-critical
        }
    }

    /**
     * Play an error sound.
     */
    fun playErrorSound() {
        try {
            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            playUri(uri)
        } catch (e: Exception) {
            // Silently fail
        }
    }

    /**
     * Play sound from URI.
     */
    private fun playUri(uri: Uri) {
        release()
        mediaPlayer = MediaPlayer.create(context, uri)?.apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            setOnCompletionListener { release() }
            start()
        }
    }

    /**
     * Release resources.
     */
    fun release() {
        try {
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (e: Exception) {
            // Ignore
        }
    }
}
