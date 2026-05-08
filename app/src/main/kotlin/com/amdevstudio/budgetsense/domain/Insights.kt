package com.amdevstudio.budgetsense.domain

import com.amdevstudio.budgetsense.data.local.TransactionType
import com.amdevstudio.budgetsense.data.local.entity.SavingsContributionEntity
import com.amdevstudio.budgetsense.data.local.entity.TransactionEntity

object Insights {
    fun build(
        thisWeekExpenses: List<TransactionEntity>,
        lastWeekExpenses: List<TransactionEntity>,
        monthExpenses: List<TransactionEntity>,
        categoryCaps: Map<String, Long>,
        currencyCode: String,
        hideMoney: Boolean,
    ): List<String> {
        val out = mutableListOf<String>()
        if (hideMoney) {
            out += "Turn off hide balance in settings to see amount-based tips."
            return out
        }

        fun sum(list: List<TransactionEntity>) = list.sumOf { it.amountCents }

        val foodWeek = thisWeekExpenses.filter { it.category == "Food" }.sumOf { it.amountCents }
        val foodLast = lastWeekExpenses.filter { it.category == "Food" }.sumOf { it.amountCents }
        if (foodWeek > 0 && foodWeek > foodLast * 11 / 10) {
            out += "You spent more on food this week than last week."
        }

        categoryCaps.forEach { (cat, cap) ->
            val spent = monthExpenses.filter { it.type == TransactionType.EXPENSE && it.category == cat }
                .sumOf { it.amountCents }
            if (cap > 0 && spent >= cap * 9 / 10) {
                out += "You are close to your $cat budget."
            }
        }

        val top = monthExpenses
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.category }
            .mapValues { (_, v) -> v.sumOf { it.amountCents } }
            .maxByOrNull { it.value }
        if (top != null && top.value > 0) {
            out += "Your highest expense this month is ${top.key} (${MoneyFormat.format(currencyCode, top.value, false)})."
        }

        val snacksHint = thisWeekExpenses.count { it.category == "Food" && it.note.contains("snack", true) }
        if (snacksHint >= 3) {
            out += "Try reducing snack purchases — small repeats add up fast."
        }

        if (out.isEmpty()) {
            out += "Nice — no urgent alerts. Keep logging expenses for sharper tips."
        }
        return out.take(6)
    }

    /** Tips based on savings deposits (contributions), not expense transactions. */
    fun buildSavingsInsights(
        contributions: List<SavingsContributionEntity>,
        currencyCode: String,
        hideMoney: Boolean,
    ): List<String> {
        val out = mutableListOf<String>()
        if (contributions.isEmpty()) {
            out += "No savings deposits yet — add goals and deposits in Savings to see month-to-month insights."
            return out
        }
        if (hideMoney) {
            out += "Turn off hide balance in Account to see savings amounts in Insights."
            return out
        }

        val byMonth = contributions.groupBy { Time.monthKeyFromEpochMillis(it.createdAtMillis) }
            .mapValues { (_, v) -> v.sumOf { it.amountCents } }
            .filterValues { it > 0L }

        val currentKey = Time.monthKey()
        val prevKey = Time.previousMonthKey(currentKey)
        val thisMonth = byMonth[currentKey] ?: 0L
        val lastMonth = byMonth[prevKey] ?: 0L

        if (thisMonth > 0L && lastMonth > 0L) {
            if (thisMonth > lastMonth) {
                out +=
                    "You saved more this month than last (${MoneyFormat.format(currencyCode, thisMonth, false)} vs ${MoneyFormat.format(currencyCode, lastMonth, false)})."
            } else if (lastMonth > thisMonth) {
                out +=
                    "Last month you saved more than this month so far (${MoneyFormat.format(currencyCode, lastMonth, false)} vs ${MoneyFormat.format(currencyCode, thisMonth, false)}) — there’s still time to catch up."
            }
        } else if (thisMonth > 0L && lastMonth == 0L) {
            out += "Nice — you’ve started saving this month (${MoneyFormat.format(currencyCode, thisMonth, false)} in deposits)."
        }

        val best = byMonth.maxByOrNull { it.value }
        if (best != null && best.value > 0L) {
            val label = Time.formatMonthKey(best.key)
            if (best.key != currentKey || byMonth.size > 1) {
                out +=
                    "Your biggest savings month recorded here is $label (${MoneyFormat.format(currencyCode, best.value, false)} total deposits)."
            }
        }

        val biggestDeposit = contributions.maxByOrNull { it.amountCents }
        if (biggestDeposit != null && biggestDeposit.amountCents > 0L) {
            val whenLabel = Time.formatMonthKey(Time.monthKeyFromEpochMillis(biggestDeposit.createdAtMillis))
            out +=
                "Your single largest deposit was ${MoneyFormat.format(currencyCode, biggestDeposit.amountCents, false)} ($whenLabel)."
        }

        if (out.isEmpty()) {
            out += "Keep adding deposits — Insights will highlight your best savings months."
        }
        return out.take(4)
    }
}
