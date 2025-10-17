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
    var sleep: SleepQuality = SleepQuality.MEDIUM,
    var social: SocialActivity = SocialActivity.NONE,
    var hobby: Hobby = Hobby.NONE,
    var food: FoodType = FoodType.NONE,
    var timestamp: String = LocalDateTime.now().format(
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    )
) : Parcelable
