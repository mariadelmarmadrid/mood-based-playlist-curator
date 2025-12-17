package org.wit.mood.models

import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Represents all mood entries for a single calendar day,
 * along with that day's average mood score.
 *
 * Each instance corresponds to one "day card" in the UI.
 */
data class DailyMoodSummary(
    /** Date string in format "yyyy-MM-dd". */
    val date: String,

    /** List of all MoodModel entries recorded on this date. */
    val moods: List<MoodModel>,

    /** Average numeric score for the day's moods. */
    val averageScore: Double
)