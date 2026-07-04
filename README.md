# 🎤 DJK Transcriptor

**Application Android de contrôle vocal avancée**

> Développé par **Etienne Tech**

---

## 📱 Description

DJK Transcriptor est une application Android professionnelle qui vous permet de contrôler votre téléphone uniquement par la voix. Parlez naturellement en français ou en anglais, et DJK exécute vos commandes instantanément.

## ✨ Fonctionnalités

### 📱 Gestion des Applications
- **Ouvrir** une application par son nom
- **Fermer** l'application courante ou une app spécifique

### 📂 Gestion des Fichiers
- **Supprimer** des fichiers par nom
- **Lister** les fichiers du dossier Téléchargements

### 🎬 Médias
- **Jouer** une vidéo spécifique
- **Jouer** de la musique ou un son
- **Ouvrir** la caméra pour prendre une photo
- **Ouvrir** la galerie de photos

### 📞 Communication
- **Appeler** un contact par son nom
- **Envoyer un SMS** à un contact

### ⚙️ Contrôles Système
- **Bluetooth** : Activer/Désactiver
- **WiFi** : Ouvrir les paramètres
- **Volume** : Monter/Baisse/Couper
- **Lampe torche** : Allumer/Éteindre
- **Paramètres** : Ouvrir les réglages

### 🕐 Informations
- **Heure** actuelle
- **Niveau de batterie**
- **Recherche web** vocale

### 🧭 Navigation
- Retour à l'accueil
- Écran précédent
- Applications récentes
- Défilement haut/bas

## 🎯 Commandes Vocales Exemples

```
"Ouvre WhatsApp"
"Ferme Instagram"
"Supprime le fichier photo.jpg"
"Joue la vidéo vacances"
"Joue la musique africaine"
"Appelle Maman"
"Envoie un SMS à Papa"
"Active Bluetooth"
"Monte le volume"
"Quelle heure est-il ?"
"Niveau de batterie"
"Cherche recettes de cuisine"
"Aide"
```

## 🏗️ Architecture

```
com.etienetech.djktranscriptor/
├── data/           # Modèles de données
│   ├── ChatMessage.kt
│   ├── CommandResult.kt
│   └── HistoryItem.kt
├── engine/         # Moteur de commandes
│   ├── CommandParser.kt      # Analyse du langage naturel
│   ├── CommandExecutor.kt    # Exécution des commandes
│   └── VoiceRecognizer.kt    # Reconnaissance vocale
├── ui/             # Interface utilisateur
│   ├── SplashActivity.kt
│   ├── OnboardingActivity.kt
│   ├── MainActivity.kt
│   └── ConversationAdapter.kt
├── service/        # Services Android
│   └── VoiceListenerService.kt
└── util/           # Utilitaires
    ├── PrefsManager.kt
    ├── SoundPlayer.kt
    └── VibrationHelper.kt
```

## 🚀 Compilation

### Prérequis
- Android Studio Hedgehog (2023.1.1) ou supérieur
- JDK 17
- Android SDK 34

### Compilation locale
```bash
./gradlew assembleDebug     # APK Debug
./gradlew assembleRelease   # APK Release
```

### Compilation via GitHub Actions
1. Poussez le code sur GitHub
2. Le workflow se lance automatiquement
3. Récupérez l'APK dans l'onglet **Actions** > **Artifacts**

### Créer une Release
1. Créez un tag : `git tag v1.0.0`
2. Poussez le tag : `git push origin v1.0.0`
3. La release est créée automatiquement avec l'APK

## 📋 Permissions Requises

| Permission | Raison |
|-----------|--------|
| `RECORD_AUDIO` | Reconnaissance vocale |
| `READ/WRITE_EXTERNAL_STORAGE` | Gestion des fichiers |
| `READ_CONTACTS` | Recherche de contacts |
| `CALL_PHONE` | Passer des appels |
| `SEND_SMS` | Envoyer des SMS |
| `CAMERA` | Prendre des photos |
| `INTERNET` | Recherche web, reconnaissance vocale |

## 🎨 Design

- Interface Material Design 3
- Thème sombre professionnel (Dark Blue/Purple)
- Animations fluides et feedback haptique
- Design responsive pour toutes les tailles d'écran

## 📄 Licence

© 2024 Etienne Tech. Tous droits réservés.

---

**DJK Transcriptor** - *Your Voice, Your Commands* 🎤
