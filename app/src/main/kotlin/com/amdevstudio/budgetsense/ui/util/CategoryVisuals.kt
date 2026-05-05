package com.amdevstudio.budgetsense.ui.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.LocalMovies
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Category
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class CategoryVisual(
    val icon: ImageVector,
    val accent: Color,
)

fun expenseCategoryVisual(name: String, fallbackAccent: Color): CategoryVisual {
    val accent = when (name) {
        "Food" -> Color(0xFFFF8A65)
        "Transport" -> Color(0xFF4FC3F7)
        "Bills" -> Color(0xFF9575CD)
        "School" -> Color(0xFF64B5F6)
        "Shopping" -> Color(0xFFF06292)
        "Health" -> Color(0xFF81C784)
        "Savings" -> Color(0xFFFFB74D)
        "Entertainment" -> Color(0xFFBA68C8)
        "Others" -> Color(0xFF90A4AE)
        else -> fallbackAccent
    }
    val icon = when (name) {
        "Food" -> Icons.Default.Restaurant
        "Transport" -> Icons.Default.DirectionsCar
        "Bills" -> Icons.Default.CreditCard
        "School" -> Icons.Default.School
        "Shopping" -> Icons.Default.ShoppingBag
        "Health" -> Icons.Default.HealthAndSafety
        "Savings" -> Icons.Default.Savings
        "Entertainment" -> Icons.Default.LocalMovies
        "Others" -> Icons.Default.MoreHoriz
        else -> Icons.Default.Category
    }
    return CategoryVisual(
        icon = icon,
        accent = accent,
    )
}
