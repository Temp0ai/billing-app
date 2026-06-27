# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Google API
-keep class com.google.api.services.drive.** { *; }
-keep class com.google.api.client.** { *; }

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Compose
-dontwarn androidx.compose.**

# Apache HTTP
-dontwarn javax.naming.**
-dontwarn javax.security.**
-dontwarn org.ietf.jgss.**
-dontwarn org.apache.http.**
-keep class org.apache.http.** { *; }

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Data entities for Room
-keep class com.billing.app.data.entity.** { *; }

# App models
-keep class com.billing.app.data.** { *; }
