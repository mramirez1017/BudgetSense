plugins {
    id("com.android.application") version "8.7.2" apply false
    // Kotlin 2.2+ required: Firebase Analytics → play-services-measurement uses Kotlin 2.2 metadata
    id("org.jetbrains.kotlin.android") version "2.2.10" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.10" apply false
    id("com.google.gms.google-services") version "4.4.4" apply false
    id("com.google.devtools.ksp") version "2.2.10-2.0.2" apply false
}

