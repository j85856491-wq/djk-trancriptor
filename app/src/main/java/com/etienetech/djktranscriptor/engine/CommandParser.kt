package com.etienetech.djktranscriptor.engine

import com.etienetech.djktranscriptor.data.CommandType

/**
 * Parses natural language voice commands and maps them to [CommandType].
 *
 * Supports French and English commands.
 *
 * DJK Transcriptor - Developed by Etienne Tech
 */
object CommandParser {

    /**
     * Parse the voice input text and return a [ParsedCommand] with
     * the detected [CommandType] and extracted parameters.
     */
    fun parse(input: String): ParsedCommand {
        val text = input.trim().lowercase()

        // Greetings
        if (matchesAny(text, listOf(
                "salut", "bonjour", "hello", "hi", "hey", "coucou",
                "bonsoir", "bonne nuit", "yo", "slt"
            ))) {
            return ParsedCommand(CommandType.GREETING, text)
        }

        // Help
        if (matchesAny(text, listOf(
                "aide", "help", "que peux-tu faire", "que peux tu faire",
                "tes commandes", "tes fonctions", "what can you do",
                "commandes disponibles", "fonctionnalités"
            ))) {
            return ParsedCommand(CommandType.HELP, text)
        }

        // Open App
        if (matchesAny(text, listOf(
                "ouvre", "ouvrir", "lance", "lancer", "démarre", "démarrer",
                "open", "launch", "start", "va sur", "aller sur"
            ))) {
            val appName = extractAppName(text, listOf(
                "ouvre", "ouvrir", "lance", "lancer", "démarre", "démarrer",
                "open", "launch", "start", "va sur", "aller sur", "l'application",
                "l application", "l'app", "l app", "l'appli", "l appli"
            ))
            return ParsedCommand(CommandType.OPEN_APP, appName)
        }

        // Close App
        if (matchesAny(text, listOf(
                "ferme", "fermer", "arrête", "arrêter", "quitte", "quitter",
                "close", "stop", "kill", "tue", "terminer"
            ))) {
            val appName = extractAppName(text, listOf(
                "ferme", "fermer", "arrête", "arrêter", "quitte", "quitter",
                "close", "stop", "kill", "tue", "terminer", "l'application",
                "l application", "l'app", "l app", "l'appli", "l appli"
            ))
            return ParsedCommand(CommandType.CLOSE_APP, appName)
        }

        // Delete File
        if (matchesAny(text, listOf(
                "supprime", "supprimer", "efface", "effacer", "enlève", "enlever",
                "delete", "remove", "erase", "retire", "retirer"
            ))) {
            if (matchesAny(text, listOf("fichier", "file", "dossier", "folder",
                    "photo", "image", "vidéo", "video", "musique", "music",
                    "document", "pdf", ".jpg", ".png", ".mp4", ".mp3"))) {
                val fileName = extractFileName(text)
                return ParsedCommand(CommandType.DELETE_FILE, fileName)
            }
        }

        // Play Video
        if (matchesAny(text, listOf(
                "joue la vidéo", "joue le video", "lance la vidéo", "lance le video",
                "regarde", "play video", "watch", "montre la vidéo", "montre le video",
                "joue vidéo", "lance vidéo", "play the video"
            )) || (matchesAny(text, listOf("joue", "lance", "play")) &&
                matchesAny(text, listOf("vidéo", "video", "film", "clip")))) {
            val videoName = extractMediaName(text)
            return ParsedCommand(CommandType.PLAY_VIDEO, videoName)
        }

        // Play Music / Sound
        if (matchesAny(text, listOf(
                "joue la musique", "joue le son", "joue la chanson",
                "lance la musique", "lance le son", "lance la chanson",
                "play music", "play song", "play sound", "écoute",
                "joue musique", "joue son", "joue chanson", "mets la musique",
                "mets le son", "mets la chanson", "mettre la musique"
            )) || (matchesAny(text, listOf("joue", "lance", "play", "mets", "mettre")) &&
                matchesAny(text, listOf("musique", "son", "chanson", "music", "song", "sound", "audio")))) {
            val mediaName = extractMediaName(text)
            return ParsedCommand(CommandType.PLAY_MUSIC, mediaName)
        }

        // Call Contact
        if (matchesAny(text, listOf(
                "appelle", "appeler", "passe un appel", "call", "phone",
                "rappelle", "rappeler", "compose", "composer"
            ))) {
            val contact = extractContactName(text)
            return ParsedCommand(CommandType.CALL_CONTACT, contact)
        }

        // Send SMS
        if (matchesAny(text, listOf(
                "envoie un sms", "envoyer un sms", "sms", "message",
                "texte", "envoie un message", "envoyer un message",
                "send sms", "send message", "text"
            ))) {
            val contact = extractContactName(text)
            return ParsedCommand(CommandType.SEND_SMS, contact)
        }

        // Toggle Bluetooth
        if (matchesAny(text, listOf(
                "bluetooth", "active bluetooth", "désactive bluetooth",
                "turn on bluetooth", "turn off bluetooth", "enable bluetooth",
                "disable bluetooth"
            ))) {
            val enable = !matchesAny(text, listOf("désactive", "désactiver", "off", "disable", "éteins", "éteindre"))
            return ParsedCommand(CommandType.TOGGLE_BLUETOOTH, if (enable) "on" else "off")
        }

        // Toggle WiFi
        if (matchesAny(text, listOf(
                "wifi", "wi-fi", "active wifi", "désactive wifi",
                "turn on wifi", "turn off wifi", "enable wifi", "disable wifi",
                "internet"
            ))) {
            val enable = !matchesAny(text, listOf("désactive", "désactiver", "off", "disable", "éteins", "éteindre"))
            return ParsedCommand(CommandType.TOGGLE_WIFI, if (enable) "on" else "off")
        }

        // Toggle Flashlight
        if (matchesAny(text, listOf(
                "lampe", "torche", "flash", "lampe torche",
                "allume la lampe", "éteins la lampe", "allume le flash",
                "éteins le flash", "flashlight", "torch",
                "turn on flashlight", "turn off flashlight"
            ))) {
            val enable = !matchesAny(text, listOf("éteins", "éteindre", "off", "désactive", "ferme"))
            return ParsedCommand(CommandType.TOGGLE_FLASHLIGHT, if (enable) "on" else "off")
        }

        // Volume Up
        if (matchesAny(text, listOf(
                "monte le volume", "augmente le volume", "volume up",
                "plus fort", "plus haut", "turn up volume", "increase volume",
                "monte son", "augmente son"
            ))) {
            return ParsedCommand(CommandType.VOLUME_UP, text)
        }

        // Volume Down
        if (matchesAny(text, listOf(
                "baisse le volume", "diminue le volume", "volume down",
                "moins fort", "moins haut", "turn down volume", "decrease volume",
                "baisse son", "diminue son"
            ))) {
            return ParsedCommand(CommandType.VOLUME_DOWN, text)
        }

        // Mute
        if (matchesAny(text, listOf(
                "coupe le son", "mute", "silence", "mode silencieux",
                "couper le son", "tais-toi", "silencieux"
            ))) {
            return ParsedCommand(CommandType.MUTE, text)
        }

        // Open Camera
        if (matchesAny(text, listOf(
                "caméra", "camera", "ouvre la caméra", "prends une photo",
                "selfie", "photo", "prendre une photo", "prends un selfie",
                "open camera", "take photo", "take picture"
            ))) {
            return ParsedCommand(CommandType.OPEN_CAMERA, text)
        }

        // Screenshot
        if (matchesAny(text, listOf(
                "capture d'écran", "capture", "screenshot", "screen capture",
                "prends une capture", "capture l'écran", "prends en photo l'écran"
            ))) {
            return ParsedCommand(CommandType.SCREENSHOT, text)
        }

        // Open Settings
        if (matchesAny(text, listOf(
                "paramètres", "settings", "réglages", "ouvre les paramètres",
                "open settings", "configuration"
            ))) {
            return ParsedCommand(CommandType.OPEN_SETTINGS, text)
        }

        // Search Web
        if (matchesAny(text, listOf(
                "cherche", "chercher", "recherche", "rechercher", "search",
                "google", "cherche sur internet", "cherche sur google",
                "search for", "search on google", "search the web"
            ))) {
            val query = extractSearchQuery(text)
            return ParsedCommand(CommandType.SEARCH_WEB, query)
        }

        // Get Time
        if (matchesAny(text, listOf(
                "heure", "quelle heure", "il est quelle heure",
                "what time", "time", "l'heure", "donne l'heure",
                "tell me the time", "what time is it"
            ))) {
            return ParsedCommand(CommandType.GET_TIME, text)
        }

        // Get Battery
        if (matchesAny(text, listOf(
                "batterie", "niveau de batterie", "battery", "battery level",
                "combien de batterie", "charge", "niveau de charge",
                "how much battery"
            ))) {
            return ParsedCommand(CommandType.GET_BATTERY, text)
        }

        // Lock Screen
        if (matchesAny(text, listOf(
                "verrouille", "verrouiller", "lock", "lock screen",
                "verrouille l'écran", "verrouiller l'écran"
            ))) {
            return ParsedCommand(CommandType.LOCK_SCREEN, text)
        }

        // Open Browser
        if (matchesAny(text, listOf(
                "navigateur", "browser", "ouvre le navigateur",
                "ouvre chrome", "internet", "ouvre internet",
                "open browser", "open chrome"
            ))) {
            return ParsedCommand(CommandType.OPEN_BROWSER, text)
        }

        // Set Alarm
        if (matchesAny(text, listOf(
                "alarme", "alarm", "réveil", "mets une alarme",
                "set alarm", "set timer", "minuteur", "timer"
            ))) {
            return ParsedCommand(CommandType.SET_ALARM, text)
        }

        // Open Gallery
        if (matchesAny(text, listOf(
                "galerie", "gallery", "photos", "images",
                "ouvre la galerie", "ouvre les photos",
                "open gallery", "open photos"
            ))) {
            return ParsedCommand(CommandType.OPEN_GALLERY, text)
        }

        // List Files
        if (matchesAny(text, listOf(
                "liste les fichiers", "mes fichiers", "list files",
                "quels fichiers", "voir les fichiers", "montre les fichiers",
                "show files", "file manager", "gestionnaire de fichiers"
            ))) {
            return ParsedCommand(CommandType.LIST_FILES, text)
        }

        // Navigation - Scroll Up
        if (matchesAny(text, listOf(
                "monte", "haut", "scroll up", "go up", "up",
                "défile vers le haut", "remonte"
            ))) {
            return ParsedCommand(CommandType.SCROLL_UP, text)
        }

        // Navigation - Scroll Down
        if (matchesAny(text, listOf(
                "descend", "bas", "scroll down", "go down", "down",
                "défile vers le bas"
            ))) {
            return ParsedCommand(CommandType.SCROLL_DOWN, text)
        }

        // Navigation - Go Back
        if (matchesAny(text, listOf(
                "retour", "reviens", "back", "go back", "précédent",
                "retour en arrière", "va en arrière"
            ))) {
            return ParsedCommand(CommandType.GO_BACK, text)
        }

        // Navigation - Go Home
        if (matchesAny(text, listOf(
                "accueil", "home", "va à l'accueil", "écran d'accueil",
                "go home", "home screen", "menu principal"
            ))) {
            return ParsedCommand(CommandType.GO_HOME, text)
        }

        // Recent Apps
        if (matchesAny(text, listOf(
                "apps récentes", "applications récentes", "recent apps",
                "multi-tâche", "multitâche", "switch apps",
                "changer d'application", "app récente"
            ))) {
            return ParsedCommand(CommandType.RECENT_APPS, text)
        }

        return ParsedCommand(CommandType.UNKNOWN, text)
    }

    private fun matchesAny(text: String, keywords: List<String>): Boolean {
        return keywords.any { text.contains(it) }
    }

    private fun extractAppName(text: String, prefixes: List<String>): String {
        var cleaned = text
        for (prefix in prefixes.sortedByDescending { it.length }) {
            cleaned = cleaned.replace(prefix, "", ignoreCase = true).trim()
        }
        // Remove common filler words
        val fillers = listOf("l'application", "l application", "l'app", "l app",
            "l'appli", "l appli", "le", "la", "les", "une", "un", "de", "du", "des",
            "the", "a", "an", "please", "s'il te plaît", "stp")
        for (filler in fillers.sortedByDescending { it.length }) {
            cleaned = cleaned.replace(filler, "", ignoreCase = true).trim()
        }
        return cleaned.ifEmpty { text.trim() }
    }

    private fun extractFileName(text: String): String {
        val fillers = listOf(
            "supprime", "supprimer", "efface", "effacer", "enlève", "enlever",
            "delete", "remove", "erase", "retire", "retirer",
            "le fichier", "la fichier", "un fichier", "le dossier", "la dossier",
            "le", "la", "les", "un", "une", "de", "du", "des", "the", "a", "an",
            "please", "s'il te plaît", "stp"
        )
        var cleaned = text
        for (filler in fillers.sortedByDescending { it.length }) {
            cleaned = cleaned.replace(filler, "", ignoreCase = true).trim()
        }
        return cleaned.ifEmpty { text.trim() }
    }

    private fun extractMediaName(text: String): String {
        val fillers = listOf(
            "joue", "lance", "play", "mets", "mettre",
            "la vidéo", "le video", "la musique", "le son", "la chanson",
            "vidéo", "video", "musique", "son", "chanson", "music", "song", "sound",
            "la", "le", "les", "un", "une", "the", "a", "an",
            "please", "s'il te plaît", "stp"
        )
        var cleaned = text
        for (filler in fillers.sortedByDescending { it.length }) {
            cleaned = cleaned.replace(filler, "", ignoreCase = true).trim()
        }
        return cleaned.ifEmpty { text.trim() }
    }

    private fun extractContactName(text: String): String {
        val fillers = listOf(
            "appelle", "appeler", "passe un appel", "call", "phone",
            "rappelle", "rappeler", "compose", "composer",
            "envoie un sms", "envoyer un sms", "sms", "message",
            "envoie un message", "envoyer un message", "send sms", "send message",
            "le", "la", "les", "un", "une", "the", "a", "an",
            "à", "au", "to", "please", "s'il te plaît", "stp"
        )
        var cleaned = text
        for (filler in fillers.sortedByDescending { it.length }) {
            cleaned = cleaned.replace(filler, "", ignoreCase = true).trim()
        }
        return cleaned.ifEmpty { text.trim() }
    }

    private fun extractSearchQuery(text: String): String {
        val fillers = listOf(
            "cherche", "chercher", "recherche", "rechercher", "search",
            "google", "sur internet", "sur google", "on google", "the web",
            "for", "pour", "le", "la", "les", "un", "une", "the", "a", "an",
            "please", "s'il te plaît", "stp"
        )
        var cleaned = text
        for (filler in fillers.sortedByDescending { it.length }) {
            cleaned = cleaned.replace(filler, "", ignoreCase = true).trim()
        }
        return cleaned.ifEmpty { text.trim() }
    }
}

/**
 * Represents a parsed voice command with its type and extracted parameter.
 */
data class ParsedCommand(
    val type: CommandType,
    val parameter: String
)
