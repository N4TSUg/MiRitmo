package com.cean.miritmo.model

data class Habit(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val category: String = "",
    val frequency: String = "Diaria",
    val targetTime: String? = null,
    val repeatDays: List<Int> = emptyList(), // 1=Monday, 7=Sunday
    val createdAt: Long = System.currentTimeMillis()
)
