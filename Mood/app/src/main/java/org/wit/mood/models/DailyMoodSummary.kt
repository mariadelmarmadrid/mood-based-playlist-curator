package org.wit.mood.models

/**
 * Represents all mood entries for a single calendar day,
 * along with that day's average mood score.
 *
 * This class is generated in:
 *  - [MoodListActivity] → to group moods by date for display
 *  - [InsightsActivity] → to compute averages and charts
 *
 * Each instance corresponds to one "day card" in the UI.
 */
data class DailyMoodSummary(
    /** Date string in format "yyyy-MM-dd" (used as grouping key). */
    val date: String,
    /** List of all MoodModel entries recorded on this date. */
    val moods: List<MoodModel>,
    /** Average numeric score for the day's moods (derived from MoodType.score). */
    val averageScore: Double
)
