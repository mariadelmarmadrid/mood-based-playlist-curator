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
) {

    companion object {

        /**
         * Groups a list of moods by day and calculates daily averages.
         *
         * @param moods List of MoodModel entries
         * @return List of DailyMoodSummary sorted by date descending
         */
        fun fromMoods(moods: List<MoodModel>): List<DailyMoodSummary> {

            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

            return moods
                .groupBy { mood ->
                    // Extract yyyy-MM-dd from timestamp string
                    mood.timestamp.substring(0, 10)
                }
                .map { (date, moodsInDay) ->

                    val sortedMoods =
                        moodsInDay.sortedByDescending { it.timestamp }

                    val average =
                        if (sortedMoods.isNotEmpty())
                            sortedMoods.map { it.type.score }.average()
                        else
                            0.0

                    DailyMoodSummary(
                        date = date,
                        moods = sortedMoods,
                        averageScore = average
                    )
                }
                .sortedByDescending { it.date }
        }
    }
}
