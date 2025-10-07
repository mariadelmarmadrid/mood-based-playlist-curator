package org.wit.mood.models

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class MoodModel(
    var type: MoodType = MoodType.NEUTRAL,
    var note: String = "",
    var sleep: SleepQuality = SleepQuality.MEDIUM,
    var social: SocialActivity = SocialActivity.NONE,
    var hobby: Hobby = Hobby.NONE,
    var food: FoodType = FoodType.NONE,
    var timestamp: String = LocalDateTime.now().format(
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    )
)
