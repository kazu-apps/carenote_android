# Keep Room entities
-keep class com.carenote.app.data.local.entity.** { *; }

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ComponentSupplier { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentComponentBuilderEntryPoint { *; }

# Generic attributes needed by reflection-based libraries (Room, Hilt, etc.)
-keepattributes Signature, *Annotation*

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Domain model enums — Mapper layer uses name/valueOf for DB string serialization
-keep enum com.carenote.app.domain.model.** {
    *;
}

# Domain models — data classes used across layers
-keep class com.carenote.app.domain.model.** { *; }

# Sealed classes — Result, DomainError used in when expressions
-keep class com.carenote.app.domain.common.** { *; }

# Room TypeConverters — invoked via Room generated code
-keep class com.carenote.app.data.local.converter.** { *; }

# Data mapper classes — enum string parsing/serialization logic
-keep class com.carenote.app.data.mapper.** { *; }

# Navigation sealed class — route names used at runtime
-keep class com.carenote.app.ui.navigation.Screen { *; }
-keep class com.carenote.app.ui.navigation.Screen$* { *; }

# R8 full mode compatibility for Kotlin coroutines
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# SQLCipher
-keep,includedescriptorclasses class net.zetetic.database.** { *; }
-keep,includedescriptorclasses interface net.zetetic.database.** { *; }

# Warnings suppressed for Apache HTTP/JNDI/GSS
-dontwarn org.apache.http.**
-dontwarn javax.naming.**
-dontwarn org.ietf.jgss.**
-dontwarn org.jetbrains.annotations.**

# ========== Glance Widget ==========

# Glance Widget Receiver
-keep class com.carenote.app.ui.widget.CareNoteWidgetReceiver { *; }

# ========== Firebase ==========

# Crashlytics — preserve line numbers and source file names for readable stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Firestore Remote Models — used for document serialization
-keep class com.carenote.app.data.remote.model.** { *; }

# Firebase Messaging Service
-keep class com.carenote.app.data.service.CareNoteMessagingService { *; }

# Suppress warnings for Firebase internal classes
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# ========== Firebase Analytics ==========

# Firebase Analytics event/parameter names (string-based reflection)
-keep class com.google.firebase.analytics.** { *; }

# ========== Coil 3.x ==========

# Coil image loader (OkHttp integration, decoder reflection)
-keep class coil3.** { *; }
-dontwarn coil3.**

# ========== WorkManager + HiltWorker ==========

# Keep HiltWorker-annotated classes
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# ========== Paging 3 ==========

-keep class androidx.paging.** { *; }
-dontwarn androidx.paging.**

# ========== Security-Crypto ==========

-keep class androidx.security.crypto.** { *; }
-dontwarn androidx.security.crypto.**

# ========== Biometric ==========

-keep class androidx.biometric.** { *; }
-dontwarn androidx.biometric.**

# ========== Google Play Billing ==========

-keep class com.android.vending.billing.** { *; }
-keep class com.android.billingclient.** { *; }
-dontwarn com.android.billingclient.**

# ========== Firebase Functions ==========

# VerifiedPurchase data class — used for Cloud Functions response deserialization
-keep class com.carenote.app.data.remote.VerifiedPurchase { *; }
