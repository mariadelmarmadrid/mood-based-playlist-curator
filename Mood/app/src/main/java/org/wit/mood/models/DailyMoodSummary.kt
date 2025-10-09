package org.wit.mood.models

data class DailyMoodSummary(
    val date: String,
    val moods: List<MoodModel>,
    val averageScore: Double
)
