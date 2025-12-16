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
 * - An optional photo URI pointing to an image stored on the device
 * - An optional geographic location where the mood was recorded
 *
 * Implements [Parcelable] so it can be passed easily between Activities
 * (e.g., for editing a mood via Intents).
 */

/**
 * Represents a geographic location associated with a mood.
 *
 * Stores latitude, longitude and zoom level for map display.
 */
@Parcelize
data class Location(
    var lat: Double = 0.0,
    var lng: Double = 0.0,
    var zoom: Float = 15f
) : Parcelable

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
    var photoUri: String? = null,
    var location: Location? = null
) : Parcelable

