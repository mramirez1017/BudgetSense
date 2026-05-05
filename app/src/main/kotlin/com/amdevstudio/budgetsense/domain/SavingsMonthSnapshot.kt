package com.amdevstudio.budgetsense.domain

import com.amdevstudio.budgetsense.data.local.entity.SavingsContributionEntity
import com.amdevstudio.budgetsense.data.local.entity.SavingsGoalEntity
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.math.ceil

data class SavingsMonthSnapshot(
    val savedThisMonthCents: Long,
    val latestAmountCents: Long?,
    val latestDateMillis: Long?,
    /** Per-goal pace summed for goals with a deadline (ceil remaining / months left). */
    val suggestedMonthlyTargetCents: Long?,
    val totalSavedCents: Long,
    val combinedTargetCents: Long,
)

fun buildSavingsMonthSnapshot(
    goals: List<SavingsGoalEntity>,
    contributions: List<SavingsContributionEntity>,
    monthKey: String,
    zone: ZoneId = ZoneId.systemDefault(),
): SavingsMonthSnapshot {
    val start = Time.startOfMonthMillis(monthKey)
    val end = Time.endOfMonthMillis(monthKey)
    val savedThisMonth = contributions
        .asSequence()
        .filter { it.createdAtMillis >= start && it.createdAtMillis < end }
        .sumOf { it.amountCents }
    val latest = contributions.maxByOrNull { it.createdAtMillis }
    val totalSaved = goals.sumOf { it.savedCents }
    val combinedTarget = goals.sumOf { it.targetCents }

    val today = LocalDate.now(zone)
    var paceSum = 0L
    var anyPace = false
    for (g in goals) {
        val dl = g.deadlineMillis ?: continue
        val deadline = Instant.ofEpochMilli(dl).atZone(zone).toLocalDate()
        val remaining = (g.targetCents - g.savedCents).coerceAtLeast(0)
        if (remaining <= 0L) continue

        val months = if (!deadline.isAfter(today)) {
            1L
        } else {
            ChronoUnit.MONTHS.between(
                today.withDayOfMonth(1),
                deadline.withDayOfMonth(1),
            ).coerceAtLeast(1L)
        }
        paceSum += ceil(remaining.toDouble() / months.toDouble()).toLong()
        anyPace = true
    }

    return SavingsMonthSnapshot(
        savedThisMonthCents = savedThisMonth,
        latestAmountCents = latest?.amountCents,
        latestDateMillis = latest?.createdAtMillis,
        suggestedMonthlyTargetCents = if (anyPace) paceSum else null,
        totalSavedCents = totalSaved,
        combinedTargetCents = combinedTarget,
    )
}
