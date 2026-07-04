package com.etienetech.djktranscriptor.engine

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log

/**
 * Manages Android's SpeechRecognizer for voice input.
 *
 * DJK Transcriptor - Developed by Etienne Tech
 */
class VoiceRecognizer(
    private val context: Context,
    private val listener: VoiceRecognitionListener
) {

    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false
    private var continuousMode = false

    interface VoiceRecognitionListener {
        fun onListeningStarted()
        fun onPartialResult(text: String)
        fun onFinalResult(text: String)
        fun onError(errorMessage: String)
        fun onListeningEnded()
        fun onRmsChanged(rmsdB: Float)
    }

    /**
     * Check if speech recognition is available on this device.
     */
    fun isRecognitionAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }

    /**
     * Start listening for voice input.
     */
    fun startListening(continuous: Boolean = false) {
        continuousMode = continuous

        if (!isRecognitionAvailable()) {
            listener.onError("La reconnaissance vocale n'est pas disponible sur cet appareil.")
            return
        }

        stopListening()

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(createRecognitionListener())
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "fr-FR")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "fr-FR")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 2000L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 2000L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 5000L)
        }

        try {
            speechRecognizer?.startListening(intent)
            isListening = true
            listener.onListeningStarted()
        } catch (e: Exception) {
            Log.e(TAG, "Error starting speech recognition", e)
            listener.onError("Erreur lors du démarrage de la reconnaissance vocale: ${e.message}")
        }
    }

    /**
     * Stop listening.
     */
    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            isListening = false
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping speech recognition", e)
        }
    }

    /**
     * Cancel recognition.
     */
    fun cancel() {
        try {
            speechRecognizer?.cancel()
            isListening = false
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling speech recognition", e)
        }
    }

    /**
     * Destroy the recognizer.
     */
    fun destroy() {
        try {
            speechRecognizer?.destroy()
            speechRecognizer = null
            isListening = false
        } catch (e: Exception) {
            Log.e(TAG, "Error destroying speech recognition", e)
        }
    }

    /**
     * Check if currently listening.
     */
    fun isCurrentlyListening(): Boolean = isListening

    private fun createRecognitionListener() = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            Log.d(TAG, "Ready for speech")
        }

        override fun onBeginningOfSpeech() {
            Log.d(TAG, "Speech started")
        }

        override fun onRmsChanged(rmsdB: Float) {
            listener.onRmsChanged(rmsdB)
        }

        override fun onBufferReceived(buffer: ByteArray?) {
            // Not used
        }

        override fun onEndOfSpeech() {
            Log.d(TAG, "Speech ended")
            isListening = false
            listener.onListeningEnded()
        }

        override fun onError(error: Int) {
            isListening = false
            val errorMessage = getErrorMessage(error)
            Log.e(TAG, "Recognition error: $errorMessage ($error)")
            listener.onError(errorMessage)
            listener.onListeningEnded()

            // Auto-restart in continuous mode (except for certain errors)
            if (continuousMode && error != SpeechRecognizer.ERROR_NO_MATCH &&
                error != SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
                // Delay before restart
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    startListening(true)
                }, 500)
            }
        }

        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                val bestResult = matches[0]
                Log.d(TAG, "Final result: $bestResult")
                listener.onFinalResult(bestResult)
            }
            listener.onListeningEnded()

            // Auto-restart in continuous mode
            if (continuousMode) {
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    startListening(true)
                }, 300)
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                listener.onPartialResult(matches[0])
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {
            // Not used
        }
    }

    private fun getErrorMessage(error: Int): String {
        return when (error) {
            SpeechRecognizer.ERROR_AUDIO -> "Erreur audio. Vérifiez le microphone."
            SpeechRecognizer.ERROR_CLIENT -> "Erreur du client."
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Permission microphone refusée."
            SpeechRecognizer.ERROR_NETWORK -> "Erreur réseau. Vérifiez votre connexion."
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Timeout réseau."
            SpeechRecognizer.ERROR_NO_MATCH -> "Aucune correspondance trouvée."
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Reconnaissance vocale occupée."
            SpeechRecognizer.ERROR_SERVER -> "Erreur du serveur."
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Aucune parole détectée."
            else -> "Erreur inconnue ($error)."
        }
    }

    companion object {
        private const val TAG = "VoiceRecognizer"
    }
}
