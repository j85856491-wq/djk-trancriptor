package com.etienetech.djktranscriptor.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.etienetech.djktranscriptor.R
import com.etienetech.djktranscriptor.data.ChatMessage

/**
 * RecyclerView adapter for displaying conversation messages.
 *
 * DJK Transcriptor - Developed by Etienne Tech
 */
class ConversationAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val messages = mutableListOf<ChatMessage>()

    companion object {
        private const val VIEW_TYPE_USER = 0
        private const val VIEW_TYPE_SYSTEM = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].isUser) VIEW_TYPE_USER else VIEW_TYPE_SYSTEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_USER) {
            val view = inflater.inflate(R.layout.item_message_user, parent, false)
            UserMessageViewHolder(view)
        } else {
            val view = inflater.inflate(R.layout.item_message_system, parent, false)
            SystemMessageViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        when (holder) {
            is UserMessageViewHolder -> holder.bind(message)
            is SystemMessageViewHolder -> holder.bind(message)
        }
    }

    override fun getItemCount() = messages.size

    /**
     * Add a new message to the conversation.
     */
    fun addMessage(message: ChatMessage) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }

    /**
     * Update the last message (useful for partial results).
     */
    fun updateLastMessage(message: ChatMessage) {
        if (messages.isNotEmpty()) {
            messages[messages.size - 1] = message
            notifyItemChanged(messages.size - 1)
        }
    }

    /**
     * Clear all messages.
     */
    fun clearMessages() {
        messages.clear()
        notifyDataSetChanged()
    }

    /**
     * Get the last message.
     */
    fun getLastMessage(): ChatMessage? = messages.lastOrNull()

    /**
     * Get all messages.
     */
    fun getMessages(): List<ChatMessage> = messages.toList()

    // ==================== ViewHolders ====================

    inner class UserMessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvMessage: TextView = view.findViewById(R.id.tvMessage)
        private val tvTimestamp: TextView = view.findViewById(R.id.tvTimestamp)

        fun bind(message: ChatMessage) {
            tvMessage.text = message.content
            tvTimestamp.text = message.getFormattedTime()
        }
    }

    inner class SystemMessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvIcon: TextView = view.findViewById(R.id.tvIcon)
        private val tvMessage: TextView = view.findViewById(R.id.tvMessage)
        private val tvTimestamp: TextView = view.findViewById(R.id.tvTimestamp)

        fun bind(message: ChatMessage) {
            val icon = message.commandType?.icon ?: "🤖"
            tvIcon.text = icon
            tvMessage.text = message.content
            tvTimestamp.text = message.getFormattedTime()

            // Color code success/error
            when (message.isSuccess) {
                true -> {
                    tvMessage.setTextColor(itemView.context.getColor(R.color.success))
                }
                false -> {
                    tvMessage.setTextColor(itemView.context.getColor(R.color.error))
                }
                null -> {
                    tvMessage.setTextColor(itemView.context.getColor(R.color.text_primary))
                }
            }
        }
    }
}
