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
    var id: Long = 0L,
    var type: MoodType = MoodType.NEUTRAL,
    var note: String = "",
    var sleep: SleepQuality? = null,
    var social: SocialActivity? = null,
    var hobby: Hobby? = null,
    var food: FoodType? = null,
    var timestamp: String = LocalDateTime.now()
        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
    var photoUri: String? = null
) : Parcelable

