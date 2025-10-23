package org.wit.mood.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Data class representing a single mood entry recorded by the user.
 *
 * Each MoodModel stores:
 * - The main mood type (e.g., Happy, Sad, Relaxed)
 * - Optional contextual details (sleep, social activity, hobby, food)
 * - A short user note
 * - A timestamp indicating when the mood was created
 *
 * Implements [Parcelable] so it can be passed easily between Activities
 * (e.g., for editing a mood via Intents).
 */
@Parcelize
data class MoodModel(
    /** Unique ID for the mood (assigned by the data store). */
    var id: Long = 0L,
    /** The main mood selected by the user. Default = NEUTRAL. */
    var type: MoodType = MoodType.NEUTRAL,
    /** Optional free-text note entered by the user. */
    var note: String = "",

    // --- Optional context fields (nullable = "not selected") ---
    var sleep: SleepQuality? = null,
    var social: SocialActivity? = null,
    var hobby: Hobby? = null,
    var food: FoodType? = null,

    /**
     * Timestamp of when this mood entry was created.
     * Stored as a formatted string: "yyyy-MM-dd HH:mm:ss".
     */
    var timestamp: String = LocalDateTime.now().format(
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    )
) : Parcelable
