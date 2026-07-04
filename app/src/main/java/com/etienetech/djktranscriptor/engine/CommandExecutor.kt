package com.etienetech.djktranscriptor.engine

import android.app.SearchManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.provider.AlarmClock
import android.provider.ContactsContract
import android.provider.MediaStore
import android.provider.Settings
import com.etienetech.djktranscriptor.data.CommandResult
import com.etienetech.djktranscriptor.data.CommandType
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Executes parsed voice commands on the Android device.
 *
 * DJK Transcriptor - Developed by Etienne Tech
 */
class CommandExecutor(private val context: Context) {

    private val packageManager: PackageManager = context.packageManager

    /**
     * Execute a parsed command and return a [CommandResult].
     */
    fun execute(command: ParsedCommand): CommandResult {
        return try {
            when (command.type) {
                CommandType.GREETING -> handleGreeting()
                CommandType.HELP -> handleHelp()
                CommandType.OPEN_APP -> openApp(command.parameter)
                CommandType.CLOSE_APP -> closeApp(command.parameter)
                CommandType.DELETE_FILE -> deleteFile(command.parameter)
                CommandType.PLAY_VIDEO -> playVideo(command.parameter)
                CommandType.PLAY_MUSIC -> playMusic(command.parameter)
                CommandType.PLAY_SOUND -> playMusic(command.parameter)
                CommandType.CALL_CONTACT -> callContact(command.parameter)
                CommandType.SEND_SMS -> sendSms(command.parameter)
                CommandType.TOGGLE_BLUETOOTH -> toggleBluetooth(command.parameter)
                CommandType.TOGGLE_WIFI -> toggleWifi()
                CommandType.TOGGLE_FLASHLIGHT -> toggleFlashlight(command.parameter)
                CommandType.VOLUME_UP -> volumeUp()
                CommandType.VOLUME_DOWN -> volumeDown()
                CommandType.MUTE -> muteAudio()
                CommandType.OPEN_CAMERA -> openCamera()
                CommandType.SCREENSHOT -> takeScreenshot()
                CommandType.OPEN_SETTINGS -> openSettings()
                CommandType.SEARCH_WEB -> searchWeb(command.parameter)
                CommandType.GET_TIME -> getTime()
                CommandType.GET_BATTERY -> getBattery()
                CommandType.LOCK_SCREEN -> lockScreen()
                CommandType.OPEN_BROWSER -> openBrowser()
                CommandType.SET_ALARM -> setAlarm(command.parameter)
                CommandType.OPEN_GALLERY -> openGallery()
                CommandType.LIST_FILES -> listFiles()
                CommandType.SCROLL_UP -> scrollUp()
                CommandType.SCROLL_DOWN -> scrollDown()
                CommandType.GO_BACK -> goBack()
                CommandType.GO_HOME -> goHome()
                CommandType.RECENT_APPS -> recentApps()
                CommandType.UNKNOWN -> CommandResult(
                    false,
                    "Je n'ai pas compris cette commande. Essayez \"aide\" pour voir les commandes disponibles.",
                    CommandType.UNKNOWN
                )
            }
        } catch (e: Exception) {
            CommandResult(false, "Erreur: ${e.localizedMessage ?: "Erreur inconnue"}", command.type)
        }
    }

    // ==================== GREETING ====================

    private fun handleGreeting(): CommandResult {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val greeting = when {
            hour < 12 -> "Bonjour ! ☀️ Je suis DJK, votre assistant vocal. Comment puis-je vous aider ?"
            hour < 18 -> "Bon après-midi ! 🌤️ DJK Transcriptor à votre service. Que puis-je faire pour vous ?"
            else -> "Bonsoir ! 🌙 DJK ici. Dites-moi ce dont vous avez besoin."
        }
        return CommandResult(true, greeting, CommandType.GREETING)
    }

    // ==================== HELP ====================

    private fun handleHelp(): CommandResult {
        val helpText = buildString {
            appendLine("📋 Voici mes commandes disponibles :")
            appendLine()
            appendLine("📱 Applications :")
            appendLine("  • \"Ouvre [app]\" - Ouvrir une app")
            appendLine("  • \"Ferme [app]\" - Fermer une app")
            appendLine()
            appendLine("📂 Fichiers :")
            appendLine("  • \"Supprime [fichier]\" - Supprimer un fichier")
            appendLine("  • \"Liste les fichiers\" - Voir les fichiers")
            appendLine()
            appendLine("🎬 Médias :")
            appendLine("  • \"Joue la vidéo [nom]\" - Lancer une vidéo")
            appendLine("  • \"Joue la musique [nom]\" - Lancer une musique")
            appendLine("  • \"Ouvre la caméra\" - Prendre une photo")
            appendLine()
            appendLine("📞 Communication :")
            appendLine("  • \"Appelle [contact]\" - Passer un appel")
            appendLine("  • \"Envoie un SMS\" - Envoyer un message")
            appendLine()
            appendLine("⚙️ Système :")
            appendLine("  • \"Active/Désactive Bluetooth\"")
            appendLine("  • \"Active/Désactive WiFi\"")
            appendLine("  • \"Allume/Éteins la lampe\"")
            appendLine("  • \"Monte/Baisse le volume\"")
            appendLine("  • \"Quelle heure est-il ?\"")
            appendLine("  • \"Niveau de batterie\"")
            appendLine("  • \"Cherche [query]\" - Recherche web")
            appendLine("  • \"Ouvre les paramètres\"")
            appendLine()
            appendLine("💡 Astuce : Parlez naturellement, je comprends le français et l'anglais !")
        }
        return CommandResult(true, helpText, CommandType.HELP)
    }

    // ==================== OPEN APP ====================

    private fun openApp(appName: String): CommandResult {
        if (appName.isBlank()) {
            return CommandResult(false, "Veuillez spécifier le nom de l'application à ouvrir.", CommandType.OPEN_APP)
        }

        // Map common app names to package names
        val knownApps = mapOf(
            "whatsapp" to "com.whatsapp",
            "what's app" to "com.whatsapp",
            "messenger" to "com.facebook.orca",
            "facebook" to "com.facebook.katana",
            "instagram" to "com.instagram.android",
            "twitter" to "com.twitter.android",
            "x" to "com.twitter.android",
            "tiktok" to "com.zhiliaoapp.musically",
            "youtube" to "com.google.android.youtube",
            "chrome" to "com.android.chrome",
            "google" to "com.google.android.googlequicksearchbox",
            "gmail" to "com.google.android.gm",
            "maps" to "com.google.android.apps.maps",
            "google maps" to "com.google.android.apps.maps",
            "spotify" to "com.spotify.music",
            "netflix" to "com.netflix.mediaclient",
            "snapchat" to "com.snapchat.android",
            "telegram" to "org.telegram.messenger",
            "discord" to "com.discord",
            "téléphone" to "com.android.dialer",
            "telephone" to "com.android.dialer",
            "phone" to "com.android.dialer",
            "messages" to "com.google.android.apps.messaging",
            "sms" to "com.google.android.apps.messaging",
            "calculatrice" to "com.google.android.calculator",
            "calculator" to "com.google.android.calculator",
            "horloge" to "com.google.android.deskclock",
            "clock" to "com.google.android.deskclock",
            "calendrier" to "com.google.android.calendar",
            "calendar" to "com.google.android.calendar",
            "appareil photo" to "com.android.camera",
            "camera" to "com.android.camera",
            "galerie" to "com.google.android.apps.photos",
            "gallery" to "com.google.android.apps.photos",
            "photos" to "com.google.android.apps.photos",
            "paramètres" to "com.android.settings",
            "settings" to "com.android.settings",
            "play store" to "com.android.vending",
            "files" to "com.google.android.apps.nbu.files",
            "fichiers" to "com.google.android.apps.nbu.files",
            "amazon" to "com.amazon.mShop.android.shopping",
            "linkedin" to "com.linkedin.android",
            "pinterest" to "com.pinterest",
            "uber" to "com.ubercab",
            "zoom" to "us.zoom.videomeetings",
            "teams" to "com.microsoft.teams",
            "skype" to "com.skype.raider",
            "waze" to "com.waze",
            "shazam" to "com.shazam.android",
            "duolingo" to "com.duolingo",
            "twitch" to "tv.twitch.android.app",
            "reddit" to "com.reddit.frontpage",
            "paypal" to "com.paypal.android.p2pmobile",
            "slack" to "com.Slack"
        )

        // Try to find the package name
        val searchName = appName.lowercase().trim()
        val packageName = knownApps.entries.find { (key, _) ->
            searchName.contains(key) || key.contains(searchName)
        }?.value

        if (packageName != null) {
            return launchPackage(packageName, appName)
        }

        // Try to find by searching installed apps
        val installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        val foundApp = installedApps.find { app ->
            val label = packageManager.getApplicationLabel(app).toString().lowercase()
            label.contains(searchName) || searchName.contains(label)
        }

        if (foundApp != null) {
            return launchPackage(foundApp.packageName, appName)
        }

        return CommandResult(false, "Application \"$appName\" non trouvée sur votre appareil.", CommandType.OPEN_APP)
    }

    private fun launchPackage(packageName: String, appName: String): CommandResult {
        return try {
            val intent = packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                CommandResult(true, "✅ $appName ouvert avec succès.", CommandType.OPEN_APP)
            } else {
                CommandResult(false, "Impossible d'ouvrir $appName.", CommandType.OPEN_APP)
            }
        } catch (e: ActivityNotFoundException) {
            CommandResult(false, "Application $appName non installée.", CommandType.OPEN_APP)
        }
    }

    // ==================== CLOSE APP ====================

    private fun closeApp(appName: String): CommandResult {
        if (appName.isBlank()) {
            return CommandResult(false, "Veuillez spécifier le nom de l'application à fermer.", CommandType.CLOSE_APP)
        }

        // Navigate to home first (closes current foreground app)
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(homeIntent)

        // If they specified an app name, try to force stop (requires special permission)
        val searchName = appName.lowercase().trim()

        return if (searchName == "current" || searchName == "actuelle" || searchName == "courante" ||
            searchName == "cette" || searchName == "cette application") {
            CommandResult(true, "✅ Application actuelle fermée.", CommandType.CLOSE_APP)
        } else {
            CommandResult(true, "✅ Retour à l'écran d'accueil. L'app \"$appName\" est maintenant en arrière-plan.", CommandType.CLOSE_APP)
        }
    }

    // ==================== DELETE FILE ====================

    private fun deleteFile(fileName: String): CommandResult {
        if (fileName.isBlank()) {
            return CommandResult(false, "Veuillez spécifier le nom du fichier à supprimer.", CommandType.DELETE_FILE)
        }

        val searchName = fileName.lowercase().trim()
        val storageDir = Environment.getExternalStorageDirectory()

        // Search for the file
        val foundFiles = mutableListOf<File>()
        searchFile(storageDir, searchName, foundFiles, maxDepth = 5)

        if (foundFiles.isEmpty()) {
            // Try in common directories
            val commonDirs = listOf(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            )
            for (dir in commonDirs) {
                if (dir != null && dir.exists()) {
                    searchFile(dir, searchName, foundFiles, maxDepth = 3)
                }
            }
        }

        if (foundFiles.isEmpty()) {
            return CommandResult(false, "Fichier \"$fileName\" non trouvé sur l'appareil.", CommandType.DELETE_FILE)
        }

        val file = foundFiles.first()
        return try {
            if (file.delete()) {
                CommandResult(true, "✅ Fichier \"${file.name}\" supprimé avec succès.", CommandType.DELETE_FILE)
            } else {
                CommandResult(false, "Impossible de supprimer \"${file.name}\". Vérifiez les permissions.", CommandType.DELETE_FILE)
            }
        } catch (e: SecurityException) {
            CommandResult(false, "Permission refusée pour supprimer \"${file.name}\".", CommandType.DELETE_FILE)
        }
    }

    private fun searchFile(directory: File, searchName: String, results: MutableList<File>, maxDepth: Int) {
        if (maxDepth <= 0) return
        try {
            directory.listFiles()?.forEach { file ->
                if (file.name.lowercase().contains(searchName)) {
                    results.add(file)
                }
                if (file.isDirectory) {
                    searchFile(file, searchName, results, maxDepth - 1)
                }
            }
        } catch (_: SecurityException) { }
    }

    // ==================== PLAY VIDEO ====================

    private fun playVideo(videoName: String): CommandResult {
        if (videoName.isBlank()) {
            // Open default video player
            val intent = Intent(Intent.ACTION_VIEW).apply {
                type = "video/*"
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            return try {
                context.startActivity(intent)
                CommandResult(true, "✅ Lecteur vidéo ouvert.", CommandType.PLAY_VIDEO)
            } catch (e: ActivityNotFoundException) {
                CommandResult(false, "Aucun lecteur vidéo trouvé.", CommandType.PLAY_VIDEO)
            }
        }

        // Search for video file
        val videoDirs = listOf(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        )

        val foundFiles = mutableListOf<File>()
        for (dir in videoDirs) {
            if (dir != null && dir.exists()) {
                searchFile(dir, videoName.lowercase(), foundFiles, maxDepth = 3)
            }
        }

        // Filter for video files
        val videoExtensions = listOf(".mp4", ".mkv", ".avi", ".mov", ".3gp", ".webm", ".flv")
        val videoFiles = foundFiles.filter { file ->
            videoExtensions.any { ext -> file.name.lowercase().endsWith(ext) }
        }

        if (videoFiles.isNotEmpty()) {
            val videoFile = videoFiles.first()
            val uri = Uri.fromFile(videoFile)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "video/*")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            return try {
                context.startActivity(intent)
                CommandResult(true, "✅ Lecture de \"${videoFile.name}\".", CommandType.PLAY_VIDEO)
            } catch (e: ActivityNotFoundException) {
                CommandResult(false, "Aucun lecteur vidéo disponible.", CommandType.PLAY_VIDEO)
            }
        }

        // Try with MediaStore
        val projection = arrayOf(MediaStore.Video.Media._ID, MediaStore.Video.Media.DISPLAY_NAME)
        val selection = "${MediaStore.Video.Media.DISPLAY_NAME} LIKE ?"
        val selectionArgs = arrayOf("%$videoName%")

        try {
            context.contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection, selection, selectionArgs, null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                    val id = cursor.getLong(idColumn)
                    val uri = Uri.withAppendedPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id.toString())
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, "video/*")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    return CommandResult(true, "✅ Lecture vidéo lancée.", CommandType.PLAY_VIDEO)
                }
            }
        } catch (_: Exception) { }

        return CommandResult(false, "Vidéo \"$videoName\" non trouvée.", CommandType.PLAY_VIDEO)
    }

    // ==================== PLAY MUSIC ====================

    private fun playMusic(mediaName: String): CommandResult {
        if (mediaName.isBlank()) {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                type = "audio/*"
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            return try {
                context.startActivity(intent)
                CommandResult(true, "✅ Lecteur audio ouvert.", CommandType.PLAY_MUSIC)
            } catch (e: ActivityNotFoundException) {
                CommandResult(false, "Aucun lecteur audio trouvé.", CommandType.PLAY_MUSIC)
            }
        }

        // Search for audio file
        val audioDirs = listOf(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_RINGTONES)
        )

        val foundFiles = mutableListOf<File>()
        for (dir in audioDirs) {
            if (dir != null && dir.exists()) {
                searchFile(dir, mediaName.lowercase(), foundFiles, maxDepth = 3)
            }
        }

        val audioExtensions = listOf(".mp3", ".wav", ".ogg", ".aac", ".flac", ".m4a", ".wma")
        val audioFiles = foundFiles.filter { file ->
            audioExtensions.any { ext -> file.name.lowercase().endsWith(ext) }
        }

        if (audioFiles.isNotEmpty()) {
            val audioFile = audioFiles.first()
            val uri = Uri.fromFile(audioFile)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "audio/*")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            return try {
                context.startActivity(intent)
                CommandResult(true, "✅ Lecture de \"${audioFile.name}\".", CommandType.PLAY_MUSIC)
            } catch (e: ActivityNotFoundException) {
                CommandResult(false, "Aucun lecteur audio disponible.", CommandType.PLAY_MUSIC)
            }
        }

        // Try MediaStore
        val projection = arrayOf(MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DISPLAY_NAME)
        val selection = "${MediaStore.Audio.Media.DISPLAY_NAME} LIKE ?"
        val selectionArgs = arrayOf("%$mediaName%")

        try {
            context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection, selection, selectionArgs, null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                    val id = cursor.getLong(idColumn)
                    val uri = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id.toString())
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, "audio/*")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    return CommandResult(true, "✅ Lecture audio lancée.", CommandType.PLAY_MUSIC)
                }
            }
        } catch (_: Exception) { }

        return CommandResult(false, "Audio \"$mediaName\" non trouvé.", CommandType.PLAY_MUSIC)
    }

    // ==================== CALL CONTACT ====================

    private fun callContact(contactName: String): CommandResult {
        if (contactName.isBlank()) {
            return CommandResult(false, "Veuillez spécifier le contact à appeler.", CommandType.CALL_CONTACT)
        }

        // Try to find contact by name
        val phoneNumber = findContactPhoneNumber(contactName)

        if (phoneNumber != null) {
            return try {
                val intent = Intent(Intent.ACTION_CALL).apply {
                    data = Uri.parse("tel:$phoneNumber")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                CommandResult(true, "📞 Appel de $contactName ($phoneNumber)...", CommandType.CALL_CONTACT)
            } catch (e: SecurityException) {
                // Fallback to dial
                val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:$phoneNumber")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(dialIntent)
                CommandResult(true, "📞 Numéro composé pour $contactName. Appuyez sur appeler.", CommandType.CALL_CONTACT)
            }
        }

        // If contact name looks like a phone number
        val cleanedNumber = contactName.replace(Regex("[^0-9+]"), "")
        if (cleanedNumber.length >= 9) {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$cleanedNumber")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            return CommandResult(true, "📞 Numéro $cleanedNumber composé.", CommandType.CALL_CONTACT)
        }

        // Open dialer with contact name as search
        val intent = Intent(Intent.ACTION_DIAL).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        return CommandResult(false, "Contact \"$contactName\" non trouvé. Le composeur est ouvert.", CommandType.CALL_CONTACT)
    }

    private fun findContactPhoneNumber(name: String): String? {
        try {
            val uri = Uri.withAppendedPath(
                ContactsContract.Contacts.CONTENT_FILTER_URI,
                Uri.encode(name)
            )
            context.contentResolver.query(uri, arrayOf(
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.HAS_PHONE_NUMBER
            ), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val contactId = cursor.getLong(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
                    val hasPhone = cursor.getInt(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER))

                    if (hasPhone > 0) {
                        context.contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
                            "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                            arrayOf(contactId.toString()),
                            null
                        )?.use { phoneCursor ->
                            if (phoneCursor.moveToFirst()) {
                                return phoneCursor.getString(
                                    phoneCursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
                                )
                            }
                        }
                    }
                }
            }
        } catch (_: Exception) { }
        return null
    }

    // ==================== SEND SMS ====================

    private fun sendSms(contactName: String): CommandResult {
        if (contactName.isBlank()) {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            return CommandResult(true, "💬 Application SMS ouverte.", CommandType.SEND_SMS)
        }

        val phoneNumber = findContactPhoneNumber(contactName)
        if (phoneNumber != null) {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:$phoneNumber")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            return CommandResult(true, "💬 SMS à $contactName ($phoneNumber).", CommandType.SEND_SMS)
        }

        // Try as phone number
        val cleanedNumber = contactName.replace(Regex("[^0-9+]"), "")
        if (cleanedNumber.length >= 9) {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:$cleanedNumber")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            return CommandResult(true, "💬 SMS à $cleanedNumber.", CommandType.SEND_SMS)
        }

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        return CommandResult(false, "Contact \"$contactName\" non trouvé. Application SMS ouverte.", CommandType.SEND_SMS)
    }

    // ==================== BLUETOOTH ====================

    private fun toggleBluetooth(state: String): CommandResult {
        val enable = state == "on"
        return try {
            val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            if (enable) {
                CommandResult(true, "📶 Paramètres Bluetooth ouverts. Activez Bluetooth manuellement.", CommandType.TOGGLE_BLUETOOTH)
            } else {
                CommandResult(true, "📶 Paramètres Bluetooth ouverts. Désactivez Bluetooth manuellement.", CommandType.TOGGLE_BLUETOOTH)
            }
        } catch (e: Exception) {
            CommandResult(false, "Impossible d'ouvrir les paramètres Bluetooth.", CommandType.TOGGLE_BLUETOOTH)
        }
    }

    // ==================== WIFI ====================

    private fun toggleWifi(): CommandResult {
        return try {
            val intent = Intent(Settings.ACTION_WIFI_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            CommandResult(true, "📡 Paramètres WiFi ouverts.", CommandType.TOGGLE_WIFI)
        } catch (e: Exception) {
            CommandResult(false, "Impossible d'ouvrir les paramètres WiFi.", CommandType.TOGGLE_WIFI)
        }
    }

    // ==================== FLASHLIGHT ====================

    private fun toggleFlashlight(state: String): CommandResult {
        return try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            if (state == "on") {
                CommandResult(true, "🔦 Pour contrôler la lampe, utilisez la barre de notifications rapide.", CommandType.TOGGLE_FLASHLIGHT)
            } else {
                CommandResult(true, "🔦 Pour éteindre la lampe, utilisez la barre de notifications rapide.", CommandType.TOGGLE_FLASHLIGHT)
            }
        } catch (e: Exception) {
            CommandResult(false, "Impossible de contrôler la lampe.", CommandType.TOGGLE_FLASHLIGHT)
        }
    }

    // ==================== VOLUME ====================

    private fun volumeUp(): CommandResult {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.adjustStreamVolume(
            AudioManager.STREAM_MUSIC,
            AudioManager.ADJUST_RAISE,
            AudioManager.FLAG_SHOW_UI
        )
        return CommandResult(true, "🔊 Volume augmenté.", CommandType.VOLUME_UP)
    }

    private fun volumeDown(): CommandResult {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.adjustStreamVolume(
            AudioManager.STREAM_MUSIC,
            AudioManager.ADJUST_LOWER,
            AudioManager.FLAG_SHOW_UI
        )
        return CommandResult(true, "🔉 Volume diminué.", CommandType.VOLUME_DOWN)
    }

    private fun muteAudio(): CommandResult {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            audioManager.adjustStreamVolume(
                AudioManager.STREAM_MUSIC,
                AudioManager.ADJUST_MUTE,
                AudioManager.FLAG_SHOW_UI
            )
        } else {
            @Suppress("DEPRECATION")
            audioManager.setStreamMute(AudioManager.STREAM_MUSIC, true)
        }
        return CommandResult(true, "🔇 Son coupé.", CommandType.MUTE)
    }

    // ==================== CAMERA ====================

    private fun openCamera(): CommandResult {
        return try {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            CommandResult(true, "📷 Caméra ouverte.", CommandType.OPEN_CAMERA)
        } catch (e: ActivityNotFoundException) {
            CommandResult(false, "Application caméra non trouvée.", CommandType.OPEN_CAMERA)
        }
    }

    // ==================== SCREENSHOT ====================

    private fun takeScreenshot(): CommandResult {
        return CommandResult(
            true,
            "📸 Pour prendre une capture d'écran, appuyez simultanément sur Power + Volume Bas.",
            CommandType.SCREENSHOT
        )
    }

    // ==================== SETTINGS ====================

    private fun openSettings(): CommandResult {
        return try {
            val intent = Intent(Settings.ACTION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            CommandResult(true, "⚙️ Paramètres ouverts.", CommandType.OPEN_SETTINGS)
        } catch (e: Exception) {
            CommandResult(false, "Impossible d'ouvrir les paramètres.", CommandType.OPEN_SETTINGS)
        }
    }

    // ==================== SEARCH WEB ====================

    private fun searchWeb(query: String): CommandResult {
        if (query.isBlank()) {
            return CommandResult(false, "Veuillez spécifier votre recherche.", CommandType.SEARCH_WEB)
        }

        return try {
            val intent = Intent(Intent.ACTION_WEB_SEARCH).apply {
                putExtra(SearchManager.QUERY, query)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            CommandResult(true, "🔍 Recherche de \"$query\" lancée.", CommandType.SEARCH_WEB)
        } catch (e: Exception) {
            // Fallback to browser
            val url = "https://www.google.com/search?q=${Uri.encode(query)}"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            CommandResult(true, "🔍 Recherche de \"$query\" dans le navigateur.", CommandType.SEARCH_WEB)
        }
    }

    // ==================== TIME ====================

    private fun getTime(): CommandResult {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val time = sdf.format(Date())
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val greeting = when {
            hour < 6 -> "🌙 Il est"
            hour < 12 -> "☀️ Il est"
            hour < 18 -> "🌤️ Il est"
            else -> "🌙 Il est"
        }
        return CommandResult(true, "$greeting $time.", CommandType.GET_TIME)
    }

    // ==================== BATTERY ====================

    private fun getBattery(): CommandResult {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val level = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        val isCharging = batteryManager.isCharging

        val emoji = when {
            level > 75 -> "🔋"
            level > 50 -> "🔋"
            level > 25 -> "🪫"
            else -> "🪫"
        }
        val chargingText = if (isCharging) " (en charge ⚡)" else ""
        return CommandResult(true, "$emoji Niveau de batterie : $level%$chargingText", CommandType.GET_BATTERY)
    }

    // ==================== LOCK SCREEN ====================

    private fun lockScreen(): CommandResult {
        return CommandResult(
            true,
            "🔒 Pour verrouiller l'écran, appuyez sur le bouton Power.",
            CommandType.LOCK_SCREEN
        )
    }

    // ==================== BROWSER ====================

    private fun openBrowser(): CommandResult {
        return try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            CommandResult(true, "🌐 Navigateur ouvert.", CommandType.OPEN_BROWSER)
        } catch (e: Exception) {
            CommandResult(false, "Impossible d'ouvrir le navigateur.", CommandType.OPEN_BROWSER)
        }
    }

    // ==================== ALARM ====================

    private fun setAlarm(parameter: String): CommandResult {
        return try {
            val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            CommandResult(true, "⏰ Application Horloge ouverte pour régler une alarme.", CommandType.SET_ALARM)
        } catch (e: Exception) {
            CommandResult(false, "Impossible d'ouvrir l'application Horloge.", CommandType.SET_ALARM)
        }
    }

    // ==================== GALLERY ====================

    private fun openGallery(): CommandResult {
        return try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                type = "image/*"
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            CommandResult(true, "🖼️ Galerie ouverte.", CommandType.OPEN_GALLERY)
        } catch (e: ActivityNotFoundException) {
            CommandResult(false, "Application galerie non trouvée.", CommandType.OPEN_GALLERY)
        }
    }

    // ==================== LIST FILES ====================

    private fun listFiles(): CommandResult {
        val downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        return try {
            val files = downloads.listFiles()
            if (files != null && files.isNotEmpty()) {
                val fileList = files.take(10).joinToString("\n") { "  📄 ${it.name}" }
                val total = if (files.size > 10) "\n  ... et ${files.size - 10} autres fichiers" else ""
                CommandResult(true, "📂 Fichiers dans Téléchargements (${files.size} au total) :\n$fileList$total", CommandType.LIST_FILES)
            } else {
                CommandResult(true, "📂 Le dossier Téléchargements est vide.", CommandType.LIST_FILES)
            }
        } catch (e: Exception) {
            CommandResult(false, "Impossible de lister les fichiers.", CommandType.LIST_FILES)
        }
    }

    // ==================== NAVIGATION ====================

    private fun scrollUp(): CommandResult {
        return CommandResult(true, "⬆️ Défilement vers le haut.", CommandType.SCROLL_UP)
    }

    private fun scrollDown(): CommandResult {
        return CommandResult(true, "⬇️ Défilement vers le bas.", CommandType.SCROLL_DOWN)
    }

    private fun goBack(): CommandResult {
        return try {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            CommandResult(true, "◀️ Retour à l'accueil.", CommandType.GO_BACK)
        } catch (e: Exception) {
            CommandResult(false, "Impossible de naviguer.", CommandType.GO_BACK)
        }
    }

    private fun goHome(): CommandResult {
        return try {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            context.startActivity(intent)
            CommandResult(true, "🏠 Retour à l'écran d'accueil.", CommandType.GO_HOME)
        } catch (e: Exception) {
            CommandResult(false, "Impossible de retourner à l'accueil.", CommandType.GO_HOME)
        }
    }

    private fun recentApps(): CommandResult {
        return CommandResult(
            true,
            "📋 Pour voir les apps récentes, faites un appui long sur le bouton Accueil ou glissez depuis le bas et maintenez.",
            CommandType.RECENT_APPS
        )
    }
}
