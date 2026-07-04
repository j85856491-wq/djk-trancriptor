package com.etienetech.djktranscriptor.data

/**
 * Represents the result of executing a voice command.
 *
 * DJK Transcriptor - Developed by Etienne Tech
 */
data class CommandResult(
    val success: Boolean,
    val message: String,
    val commandType: CommandType = CommandType.UNKNOWN,
    val details: String? = null
)

/**
 * Enumeration of all supported command types.
 */
enum class CommandType(val displayName: String, val icon: String) {
    OPEN_APP("Ouvrir App", "📱"),
    CLOSE_APP("Fermer App", "❌"),
    DELETE_FILE("Supprimer Fichier", "🗑️"),
    PLAY_VIDEO("Jouer Vidéo", "🎬"),
    PLAY_MUSIC("Jouer Musique", "🎵"),
    PLAY_SOUND("Jouer Son", "🔊"),
    CALL_CONTACT("Appeler", "📞"),
    SEND_SMS("Envoyer SMS", "💬"),
    TOGGLE_BLUETOOTH("Bluetooth", "📶"),
    TOGGLE_WIFI("WiFi", "📡"),
    TOGGLE_FLASHLIGHT("Lampe", "🔦"),
    VOLUME_UP("Monter Volume", "🔊"),
    VOLUME_DOWN("Baisser Volume", "🔉"),
    MUTE("Couper Son", "🔇"),
    OPEN_CAMERA("Caméra", "📷"),
    OPEN_SETTINGS("Paramètres", "⚙️"),
    SEARCH_WEB("Recherche Web", "🔍"),
    GET_TIME("Heure", "🕐"),
    GET_BATTERY("Batterie", "🔋"),
    SCREENSHOT("Capture Écran", "📸"),
    LOCK_SCREEN("Verrouiller", "🔒"),
    OPEN_BROWSER("Navigateur", "🌐"),
    SET_ALARM("Alarme", "⏰"),
    OPEN_GALLERY("Galerie", "🖼️"),
    LIST_FILES("Lister Fichiers", "📂"),
    SCROLL_UP("Monter", "⬆️"),
    SCROLL_DOWN("Descendre", "⬇️"),
    GO_BACK("Retour", "◀️"),
    GO_HOME("Accueil", "🏠"),
    RECENT_APPS("Apps Récentes", "📋"),
    HELP("Aide", "❓"),
    GREETING("Salutation", "👋"),
    UNKNOWN("Inconnu", "❓");
}
