package com.amdevstudio.budgetsense.domain

/** ISO 4217 codes shown in onboarding and settings. Order is display order. */
val SupportedCurrencies: List<String> =
    listOf("PHP", "USD", "EUR", "GBP", "JPY", "SGD", "AUD", "CAD", "INR", "HKD")

/**
 * Emoji flag commonly associated with each currency code (EUR → EU flag).
 * Returns null when we prefer not to show a flag for ambiguous codes.
 */
fun currencyFlagEmoji(currencyCode: String): String? = when (currencyCode.uppercase()) {
    "PHP" -> "🇵🇭"
    "USD" -> "🇺🇸"
    "EUR" -> "🇪🇺"
    "GBP" -> "🇬🇧"
    "JPY" -> "🇯🇵"
    "SGD" -> "🇸🇬"
    "AUD" -> "🇦🇺"
    "CAD" -> "🇨🇦"
    "INR" -> "🇮🇳"
    "HKD" -> "🇭🇰"
    else -> null
}

fun currencyChipLabel(code: String): String =
    currencyFlagEmoji(code)?.let { "$it $code" } ?: code
