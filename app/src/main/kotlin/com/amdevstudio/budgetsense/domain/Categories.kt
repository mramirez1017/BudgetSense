package com.amdevstudio.budgetsense.domain

import com.amdevstudio.budgetsense.data.local.TransactionType

object Categories {
    val income: List<String> = listOf(
        "Salary",
        "Allowance",
        "Freelance",
        "Business",
        "Others",
    )

    val expense: List<String> = listOf(
        "Food",
        "Transport",
        "Bills",
        "School",
        "Shopping",
        "Health",
        "Savings",
        "Entertainment",
        "Others",
    )

    fun defaultsFor(type: TransactionType): List<String> =
        when (type) {
            TransactionType.INCOME -> income
            TransactionType.EXPENSE -> expense
        }
}
