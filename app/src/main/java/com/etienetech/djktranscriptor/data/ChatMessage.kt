package com.etienetech.djktranscriptor.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Represents a message in the conversation between user and DJK.
 *
 * DJK Transcriptor - Developed by Etienne Tech
 */
data class ChatMessage(
    val id: Long = System.currentTimeMillis(),
    val content: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val commandType: CommandType? = null,
    val isSuccess: Boolean? = null
) {
    fun getFormattedTime(): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
