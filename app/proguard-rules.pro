# BudgetSense — release / Play Store

# Readable stack traces in Play Vitals when uploading mapping.txt
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses

-dontwarn com.google.android.gms.**
-dontwarn com.google.firebase.**

# kotlinx coroutines (Play Services task await)
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Room: keep entity fields stable for schema + queries (library ships rules; this is extra safety)
-keep class com.amdevstudio.budgetsense.data.local.entity.** { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }
-keep class com.amdevstudio.budgetsense.data.local.Converters { *; }
-keep class com.amdevstudio.budgetsense.data.local.TransactionType { *; }

# BuildConfig (About screen)
-keep class com.amdevstudio.budgetsense.BuildConfig { *; }
