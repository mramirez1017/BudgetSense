package com.amdevstudio.budgetsense.domain

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

object MoneyFormat {
    fun format(currencyCode: String, amountCents: Long, hide: Boolean): String {
        if (hide) return "••••"
        val currency = runCatching { Currency.getInstance(currencyCode) }.getOrElse { Currency.getInstance("USD") }
        val locale = Locale.getDefault()
        val fmt = NumberFormat.getCurrencyInstance(locale).apply {
            this.currency = currency
            maximumFractionDigits = currency.defaultFractionDigits
            minimumFractionDigits = currency.defaultFractionDigits
        }
        val major = amountCents.toDouble() / 100.0
        return fmt.format(major)
    }
}
