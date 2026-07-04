package com.etienetech.djktranscriptor.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Represents a historical command entry.
 *
 * DJK Transcriptor - Developed by Etienne Tech
 */
data class HistoryItem(
    val id: Long = System.currentTimeMillis(),
    val command: String,
    val result: String,
    val commandType: CommandType,
    val success: Boolean,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun getFormattedDate(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
