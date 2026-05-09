package com.amdevstudio.budgetsense.domain

import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

object Time {
    private val zone: ZoneId get() = ZoneId.systemDefault()
    private val monthKeyFmt = DateTimeFormatter.ofPattern("yyyy-MM")
    private val monthLabelFmt = DateTimeFormatter.ofPattern("MMM yyyy")

    fun monthKey(instant: Instant = Instant.now()): String =
        monthKeyFmt.withZone(zone).format(instant)

    /** Calendar month key (yyyy-MM) for a contribution or event timestamp. */
    fun monthKeyFromEpochMillis(millis: Long): String =
        monthKeyFmt.withZone(zone).format(Instant.ofEpochMilli(millis))

    fun formatMonthKey(monthKey: String): String {
        val parts = monthKey.split("-")
        val y = parts[0].toInt()
        val m = parts[1].toInt()
        return YearMonth.of(y, m).format(monthLabelFmt)
    }

    fun previousMonthKey(monthKey: String): String {
        val parts = monthKey.split("-")
        val y = parts[0].toInt()
        val m = parts[1].toInt()
        return YearMonth.of(y, m).minusMonths(1).format(monthKeyFmt)
    }

    fun startOfMonthMillis(monthKey: String): Long {
        val parts = monthKey.split("-")
        val y = parts[0].toInt()
        val m = parts[1].toInt()
        return LocalDate.of(y, m, 1)
            .atStartOfDay(zone)
            .toInstant()
            .toEpochMilli()
    }

    fun endOfMonthMillis(monthKey: String): Long {
        val parts = monthKey.split("-")
        val y = parts[0].toInt()
        val m = parts[1].toInt()
        val last = LocalDate.of(y, m, 1).with(TemporalAdjusters.lastDayOfMonth())
        return last.plusDays(1)
            .atStartOfDay(zone)
            .toInstant()
            .toEpochMilli()
    }

    fun startOfWeekMillis(anchor: LocalDate = LocalDate.now()): Long {
        val monday = anchor.minusDays(((anchor.dayOfWeek.value + 6) % 7).toLong())
        return monday.atStartOfDay(zone).toInstant().toEpochMilli()
    }

    fun endOfWeekMillis(anchor: LocalDate = LocalDate.now()): Long {
        val monday = anchor.minusDays(((anchor.dayOfWeek.value + 6) % 7).toLong())
        return monday.plusWeeks(1).atStartOfDay(zone).toInstant().toEpochMilli()
    }

    fun startOfWeekFromEpoch(millis: Long): Long {
        val anchor = Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()
        return startOfWeekMillis(anchor)
    }

    fun endOfWeekFromEpoch(millis: Long): Long {
        val anchor = Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()
        return endOfWeekMillis(anchor)
    }
}
