package com.amdevstudio.budgetsense.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

private const val BASE_URL = "https://api.frankfurter.app/latest"

data class FrankfurterLatestResult(
    val amountMajor: Double,
    val base: String,
    val quote: String,
    val convertedMajor: Double,
    val rateDateIso: String,
)

/**
 * Free ECB-based JSON API ([Frankfurter](https://www.frankfurter.app/docs/)).
 * Intended for illustrative conversion only; weekends may return the last business day's rate.
 */
object FrankfurterRatesClient {

    suspend fun fetchConversion(
        amountMajor: Double,
        fromCurrency: String,
        toCurrency: String,
    ): Result<FrankfurterLatestResult> = withContext(Dispatchers.IO) {
        runCatching {
            if (amountMajor <= 0.0 || fromCurrency.isBlank() || toCurrency.isBlank()) {
                throw IllegalArgumentException("Invalid amount or currency")
            }
            val from = fromCurrency.uppercase()
            val to = toCurrency.uppercase()
            if (from == to) {
                return@runCatching FrankfurterLatestResult(
                    amountMajor = amountMajor,
                    base = from,
                    quote = to,
                    convertedMajor = amountMajor,
                    rateDateIso = "",
                )
            }

            val q = buildString {
                append("amount=").append(URLEncoder.encode(amountMajor.toString(), Charsets.UTF_8.name()))
                append("&from=").append(URLEncoder.encode(from, Charsets.UTF_8.name()))
                append("&to=").append(URLEncoder.encode(to, Charsets.UTF_8.name()))
            }
            val url = URL("$BASE_URL?$q")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 12_000
                readTimeout = 15_000
                setRequestProperty("Accept", "application/json")
            }
            try {
                val code = conn.responseCode
                val stream = if (code in 200..299) conn.inputStream else conn.errorStream
                val body = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
                if (code !in 200..299) {
                    throw IllegalStateException("HTTP $code")
                }
                val json = JSONObject(body)
                val base = json.optString("base", from)
                val rateDateIso = json.optString("date", "")
                val rates = json.optJSONObject("rates") ?: throw IllegalStateException("Missing rates")
                val convertedMajor = rates.optDouble(to, Double.NaN)
                if (convertedMajor.isNaN()) throw IllegalStateException("No rate for $to")
                val inputAmount = json.optDouble("amount", amountMajor)
                FrankfurterLatestResult(
                    amountMajor = inputAmount,
                    base = base,
                    quote = to,
                    convertedMajor = convertedMajor,
                    rateDateIso = rateDateIso,
                )
            } finally {
                conn.disconnect()
            }
        }
    }
}
