package org.wit.mood.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Parcelize
data class MoodModel(
    var id: Long = 0L,
    var type: MoodType = MoodType.NEUTRAL,
    var note: String = "",

    // nullable to support "no selection"
    var sleep: SleepQuality? = null,
    var social: SocialActivity? = null,
    var hobby: Hobby? = null,
    var food: FoodType? = null,

    var timestamp: String = LocalDateTime.now().format(
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    )
) : Parcelable
