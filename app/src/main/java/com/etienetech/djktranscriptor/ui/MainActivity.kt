package com.etienetech.djktranscriptor.ui

import android.Manifest
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.etienetech.djktranscriptor.R
import com.etienetech.djktranscriptor.data.ChatMessage
import com.etienetech.djktranscriptor.data.CommandType
import com.etienetech.djktranscriptor.engine.CommandExecutor
import com.etienetech.djktranscriptor.engine.CommandParser
import com.etienetech.djktranscriptor.engine.VoiceRecognizer
import com.etienetech.djktranscriptor.util.PrefsManager
import com.etienetech.djktranscriptor.util.SoundPlayer
import com.etienetech.djktranscriptor.util.VibrationHelper
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton

/**
 * Main activity - the voice command center.
 *
 * DJK Transcriptor - Developed by Etienne Tech
 */
class MainActivity : AppCompatActivity(), VoiceRecognizer.VoiceRecognitionListener {

    // UI Components
    private lateinit var rvConversation: RecyclerView
    private lateinit var emptyStateLayout: LinearLayout
    private lateinit var fabMic: FloatingActionButton
    private lateinit var tvStatus: TextView
    private lateinit var tvTapHint: TextView
    private lateinit var voiceWaveView: View
    private lateinit var micGlow: View
    private lateinit var btnHistory: ImageButton
    private lateinit var btnInfo: ImageButton

    // Adapters & Managers
    private lateinit var conversationAdapter: ConversationAdapter
    private lateinit var prefs: PrefsManager
    private lateinit var voiceRecognizer: VoiceRecognizer
    private lateinit var commandExecutor: CommandExecutor
    private lateinit var soundPlayer: SoundPlayer
    private lateinit var vibrationHelper: VibrationHelper

    // State
    private var isListening = false
    private var isProcessing = false
    private var currentPartialText = ""
    private var voiceWaveAnimator: ObjectAnimator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        initManagers()
        setupRecyclerView()
        setupListeners()
        checkPermissions()
    }

    private fun initViews() {
        rvConversation = findViewById(R.id.rvConversation)
        emptyStateLayout = findViewById(R.id.emptyStateLayout)
        fabMic = findViewById(R.id.fabMic)
        tvStatus = findViewById(R.id.tvStatus)
        tvTapHint = findViewById(R.id.tvTapHint)
        voiceWaveView = findViewById(R.id.voiceWaveView)
        micGlow = findViewById(R.id.micGlow)
        btnHistory = findViewById(R.id.btnHistory)
        btnInfo = findViewById(R.id.btnInfo)
    }

    private fun initManagers() {
        prefs = PrefsManager(this)
        commandExecutor = CommandExecutor(this)
        soundPlayer = SoundPlayer(this)
        vibrationHelper = VibrationHelper(this)
        voiceRecognizer = VoiceRecognizer(this, this)
    }

    private fun setupRecyclerView() {
        conversationAdapter = ConversationAdapter()
        rvConversation.apply {
            layoutManager = LinearLayoutManager(this@MainActivity).apply {
                stackFromEnd = true
            }
            adapter = conversationAdapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                }
            })
        }
    }

    private fun setupListeners() {
        fabMic.setOnClickListener {
            if (isProcessing) return@setOnClickListener
            vibrationHelper.vibrateShort()

            if (isListening) {
                stopListening()
            } else {
                startListening()
            }
        }

        btnHistory.setOnClickListener {
            showHistoryDialog()
        }

        btnInfo.setOnClickListener {
            showAboutDialog()
        }

        // Long press for continuous mode
        fabMic.setOnLongClickListener {
            if (!isListening) {
                vibrationHelper.vibrateMedium()
                startListening(continuous = true)
                Toast.makeText(this, "🎤 Mode écoute continue activé", Toast.LENGTH_SHORT).show()
            }
            true
        }
    }

    private fun checkPermissions() {
        val permissions = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.RECORD_AUDIO)
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (permissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissions.toTypedArray(), 100)
        }
    }

    // ==================== VOICE CONTROL ====================

    private fun startListening(continuous: Boolean = false) {
        if (!voiceRecognizer.isRecognitionAvailable()) {
            showError("La reconnaissance vocale n'est pas disponible sur cet appareil.")
            return
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 100)
            return
        }

        voiceRecognizer.startListening(continuous)
    }

    private fun stopListening() {
        voiceRecognizer.stopListening()
    }

    // ==================== VoiceRecognitionListener ====================

    override fun onListeningStarted() {
        runOnUiThread {
            isListening = true
            currentPartialText = ""
            updateUIForListening(true)
            playStartSound()
        }
    }

    override fun onPartialResult(text: String) {
        runOnUiThread {
            currentPartialText = text
            // Show partial result in status
            tvStatus.text = "🎤 $text"
            tvStatus.visibility = View.VISIBLE
        }
    }

    override fun onFinalResult(text: String) {
        runOnUiThread {
            isListening = false
            updateUIForListening(false)

            if (text.isNotBlank()) {
                processVoiceCommand(text)
            }
        }
    }

    override fun onError(errorMessage: String) {
        runOnUiThread {
            isListening = false
            updateUIForListening(false)

            // Only show error for non-transient issues
            if (!errorMessage.contains("Aucune parole détectée") &&
                !errorMessage.contains("Aucune correspondance")) {
                showError(errorMessage)
            }
        }
    }

    override fun onListeningEnded() {
        runOnUiThread {
            isListening = false
            updateUIForListening(false)
        }
    }

    override fun onRmsChanged(rmsdB: Float) {
        runOnUiThread {
            updateVoiceWave(rmsdB)
        }
    }

    // ==================== COMMAND PROCESSING ====================

    private fun processVoiceCommand(text: String) {
        isProcessing = true
        showProcessingState()

        // Add user message to conversation
        val userMessage = ChatMessage(content = text, isUser = true)
        conversationAdapter.addMessage(userMessage)
        updateEmptyState()

        // Parse and execute
        val parsedCommand = CommandParser.parse(text)
        val result = commandExecutor.execute(parsedCommand)

        // Add system response
        val systemMessage = ChatMessage(
            content = result.message,
            isUser = false,
            commandType = result.commandType,
            isSuccess = result.success
        )
        conversationAdapter.addMessage(systemMessage)

        // Scroll to bottom
        scrollToBottom()

        // Save to history
        prefs.saveCommandToHistory(text)

        // Feedback
        if (result.success) {
            vibrationHelper.vibrateSuccess()
            if (prefs.isSoundFeedbackEnabled()) {
                soundPlayer.playSuccessSound()
            }
        } else {
            vibrationHelper.vibrateError()
            if (prefs.isSoundFeedbackEnabled()) {
                soundPlayer.playErrorSound()
            }
        }

        isProcessing = false
        hideProcessingState()
    }

    // ==================== UI UPDATES ====================

    private fun updateUIForListening(listening: Boolean) {
        if (listening) {
            // Animate mic button
            fabMic.setImageResource(R.drawable.ic_stop)
            micGlow.visibility = View.VISIBLE
            micGlow.alpha = 0f
            micGlow.animate().alpha(1f).setDuration(300).start()

            // Start glow pulse animation
            startGlowPulse()

            // Show listening status
            tvStatus.text = getString(R.string.main_listening)
            tvStatus.setBackgroundResource(R.drawable.bg_status_listening)
            tvStatus.visibility = View.VISIBLE
            tvTapHint.text = "Appuyez pour arrêter"

            // Hide empty state
            emptyStateLayout.visibility = View.GONE

        } else {
            fabMic.setImageResource(R.drawable.ic_mic)
            micGlow.visibility = View.GONE
            stopGlowPulse()
            voiceWaveAnimator?.cancel()
            voiceWaveView.alpha = 0f
            tvTapHint.text = getString(R.string.main_tap_to_speak)
        }
    }

    private fun showProcessingState() {
        tvStatus.text = getString(R.string.main_processing)
        tvStatus.setBackgroundResource(R.drawable.bg_status_processing)
        tvStatus.visibility = View.VISIBLE
    }

    private fun hideProcessingState() {
        Handler(Looper.getMainLooper()).postDelayed({
            tvStatus.visibility = View.GONE
        }, 1500)
    }

    private fun showError(message: String) {
        tvStatus.text = message
        tvStatus.setBackgroundResource(R.drawable.bg_status_error)
        tvStatus.visibility = View.VISIBLE

        Handler(Looper.getMainLooper()).postDelayed({
            tvStatus.visibility = View.GONE
        }, 3000)

        // Add error message to conversation
        val errorMessage = ChatMessage(
            content = "❌ $message",
            isUser = false,
            isSuccess = false
        )
        conversationAdapter.addMessage(errorMessage)
        scrollToBottom()
    }

    private fun updateEmptyState() {
        emptyStateLayout.visibility = if (conversationAdapter.itemCount == 0) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    private fun scrollToBottom() {
        Handler(Looper.getMainLooper()).postDelayed({
            if (conversationAdapter.itemCount > 0) {
                rvConversation.smoothScrollToPosition(conversationAdapter.itemCount - 1)
            }
        }, 100)
    }

    // ==================== ANIMATIONS ====================

    private fun startGlowPulse() {
        val animator = ObjectAnimator.ofFloat(micGlow, "alpha", 0.3f, 0.8f).apply {
            duration = 1000
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
        }
        animator.start()
        voiceWaveAnimator = animator
    }

    private fun stopGlowPulse() {
        voiceWaveAnimator?.cancel()
        micGlow.alpha = 0f
    }

    private fun updateVoiceWave(rmsdB: Float) {
        // Normalize RMS value (typically ranges from -2 to 10)
        val normalizedLevel = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)

        voiceWaveView.alpha = if (isListening) 0.8f else 0f

        // Animate the wave view scale
        val scale = 0.5f + (normalizedLevel * 0.5f)
        voiceWaveView.scaleX = scale
        voiceWaveView.scaleY = scale
    }

    // ==================== SOUND ====================

    private fun playStartSound() {
        if (prefs.isSoundFeedbackEnabled()) {
            soundPlayer.playSuccessSound()
        }
    }

    // ==================== DIALOGS ====================

    private fun showAboutDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_about, null)

        // Set version
        val tvVersion = dialogView.findViewById<TextView>(R.id.tvVersion)
        try {
            val versionName = packageManager.getPackageInfo(packageName, 0).versionName
            tvVersion.text = getString(R.string.about_version, versionName)
        } catch (e: Exception) {
            tvVersion.text = getString(R.string.about_version, "1.0.0")
        }

        MaterialAlertDialogBuilder(this, R.style.Theme_DJKTranscriptor)
            .setView(dialogView)
            .setPositiveButton(R.string.ok, null)
            .show()
    }

    private fun showHistoryDialog() {
        val history = prefs.getCommandHistory()

        if (history.isEmpty()) {
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.history_title)
                .setMessage(R.string.history_empty)
                .setPositiveButton(R.string.ok, null)
                .show()
            return
        }

        val historyArray = history.toTypedArray()

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.history_title)
            .setItems(historyArray) { _, which ->
                val selectedCommand = historyArray[which]
                // Re-execute the command
                processVoiceCommand(selectedCommand)
            }
            .setNeutralButton(R.string.history_clear) { _, _ ->
                showClearHistoryConfirmation()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showClearHistoryConfirmation() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.history_clear)
            .setMessage(R.string.history_clear_confirm)
            .setPositiveButton(R.string.yes) { _, _ ->
                prefs.clearHistory()
                Toast.makeText(this, "Historique effacé", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }

    // ==================== LIFECYCLE ====================

    override fun onDestroy() {
        super.onDestroy()
        voiceRecognizer.destroy()
        soundPlayer.release()
    }

    override fun onPause() {
        super.onPause()
        if (isListening) {
            stopListening()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, ready to listen
            } else {
                showError("Permission microphone requise pour utiliser DJK Transcriptor.")
            }
        }
    }

    override fun onBackPressed() {
        if (isListening) {
            stopListening()
        } else {
            super.onBackPressed()
        }
    }
}
