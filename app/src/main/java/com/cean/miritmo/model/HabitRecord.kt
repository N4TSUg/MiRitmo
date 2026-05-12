package com.cean.miritmo.model

data class HabitRecord(
    val id: String = "",
    val habitId: String = "",
    val date: String = "", // Formato YYYY-MM-DD
    val isCompleted: Boolean = false,
    val completedAt: Long? = null
)
