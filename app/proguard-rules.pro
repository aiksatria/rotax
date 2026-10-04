# R8 / ProGuard rules for ROTAX

# Keep DataStore preference keys and model data classes
-keep class id.web.idm.forcerotation.data.** { *; }
-keep class id.web.idm.forcerotation.domain.** { *; }
-keepclassmembers class * implements androidx.datastore.core.Serializer { *; }
-keepclassmembers class id.web.idm.forcerotation.data.** { *; }
-keepclassmembers class id.web.idm.forcerotation.domain.** { *; }

# Keep Enums
-keepclassmembers enum id.web.idm.forcerotation.domain.** { *; }

# Keep Coroutines internals
-keepclassmembers class kotlinx.coroutines.** { *; }

# Keep Compose reflection
-keepclassmembers class androidx.compose.** { *; }

# Keep Play In-App Review & In-App Update (Play Core)
-dontwarn com.google.android.gms.common.annotation.**
-dontwarn com.google.android.play.core.**
-keep class com.google.android.play.core.** { *; }
-keep class com.google.android.gms.** { *; }

