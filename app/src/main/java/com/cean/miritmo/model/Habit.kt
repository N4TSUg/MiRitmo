package com.cean.miritmo.model

data class Habit(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val category: String = "",
    val frequency: String = "Diaria",
    val targetTime: String? = null,
    val targetTimes: List<String> = emptyList(),
    val durationMinutes: Int? = null,
    val repeatDays: List<Int> = emptyList(), // 1=Monday, 7=Sunday
    val oneTime: Boolean = false,
    val oneTimeDate: String? = null,
    val currentStreak: Int = 0,
    val lastCompletedDate: String? = null,
    val completionsByDate: Map<String, Int> = emptyMap(),
    val createdAt: Long = System.currentTimeMillis()
) {
    // Helper to get all times, falling back to legacy targetTime if targetTimes is empty
    fun getEffectiveTargetTimes(): List<String> {
        if (targetTimes.isNotEmpty()) return targetTimes
        if (targetTime != null) return listOf(targetTime)
        return emptyList()
    }
}
