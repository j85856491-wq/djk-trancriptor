# DJK Transcriptor - ProGuard Rules
# Developed by Etienne Tech

# Keep application class
-keep class com.etienetech.djktranscriptor.** { *; }

# Keep data classes
-keep class com.etienetech.djktranscriptor.data.** { *; }

# Keep command engine
-keep class com.etienetech.djktranscriptor.engine.** { *; }

# Android Speech Recognition
-keep class android.speech.** { *; }
-keep interface android.speech.** { *; }

# AndroidX
-keep class androidx.** { *; }
-keep interface androidx.** { *; }

# Material Design
-keep class com.google.android.material.** { *; }

# Coroutines
-keepnames class kotlinx.coroutines.** { *; }

# Remove logging in release
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
}
