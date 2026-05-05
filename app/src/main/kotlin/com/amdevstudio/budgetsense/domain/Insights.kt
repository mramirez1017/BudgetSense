package com.amdevstudio.budgetsense.domain

import com.amdevstudio.budgetsense.data.local.TransactionType
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
}
