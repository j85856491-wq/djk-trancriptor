package com.etienetech.djktranscriptor.util

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Manages app preferences and persistent data.
 *
 * DJK Transcriptor - Developed by Etienne Tech
 */
class PrefsManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    // Onboarding
    fun isOnboardingCompleted(): Boolean = prefs.getBoolean(KEY_ONBOARDING_DONE, false)
    fun setOnboardingCompleted() = prefs.edit().putBoolean(KEY_ONBOARDING_DONE, true).apply()

    // Continuous listening
    fun isContinuousListening(): Boolean = prefs.getBoolean(KEY_CONTINUOUS_LISTENING, false)
    fun setContinuousListening(enabled: Boolean) = prefs.edit().putBoolean(KEY_CONTINUOUS_LISTENING, enabled).apply()

    // Vibration
    fun isVibrationEnabled(): Boolean = prefs.getBoolean(KEY_VIBRATION, true)
    fun setVibrationEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_VIBRATION, enabled).apply()

    // Sound feedback
    fun isSoundFeedbackEnabled(): Boolean = prefs.getBoolean(KEY_SOUND_FEEDBACK, true)
    fun setSoundFeedbackEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_SOUND_FEEDBACK, enabled).apply()

    // Command history
    fun saveCommandToHistory(command: String) {
        val history = getCommandHistory().toMutableList()
        history.add(0, command)
        // Keep last 50 commands
        if (history.size > 50) {
            history.subList(50, history.size).clear()
        }
        prefs.edit().putString(KEY_COMMAND_HISTORY, gson.toJson(history)).apply()
    }

    fun getCommandHistory(): List<String> {
        val json = prefs.getString(KEY_COMMAND_HISTORY, null) ?: return emptyList()
        val type = object : TypeToken<List<String>>() {}.type
        return try {
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun clearHistory() {
        prefs.edit().remove(KEY_COMMAND_HISTORY).apply()
    }

    companion object {
        private const val PREFS_NAME = "djk_transcriptor_prefs"
        private const val KEY_ONBOARDING_DONE = "onboarding_done"
        private const val KEY_CONTINUOUS_LISTENING = "continuous_listening"
        private const val KEY_VIBRATION = "vibration"
        private const val KEY_SOUND_FEEDBACK = "sound_feedback"
        private const val KEY_COMMAND_HISTORY = "command_history"
    }
}
