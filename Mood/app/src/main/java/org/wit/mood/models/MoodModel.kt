package org.wit.mood.models

import java.time.LocalDateTime

data class MoodModel(
    var type: MoodType = MoodType.NEUTRAL,
    var note: String = "",
    var sleep: SleepQuality = SleepQuality.MEDIUM,
    var social: SocialActivity = SocialActivity.NONE,
    var hobby: Hobby = Hobby.NONE,
    var food: FoodType = FoodType.NONE,
    var timestamp: String = LocalDateTime.now().toString()
)
