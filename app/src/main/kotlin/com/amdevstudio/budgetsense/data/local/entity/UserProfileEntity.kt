package com.amdevstudio.budgetsense.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val userId: String,
    val displayName: String,
    val currencyCode: String,
    val monthlyIncomeCents: Long,
    val onboardingComplete: Boolean,
    val hideBalance: Boolean = false,
)
