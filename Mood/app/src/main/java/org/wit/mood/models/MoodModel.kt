package org.wit.mood.models

enum class MoodType(val label: String, val score: Int) {
    Happy("Happy", 2),
    Relaxed("Relaxed", 1),
    Neutral("Neutral", 0),
    Sad("Sad", -1),
    Angry("Angry", -2)
}

data class MoodModel(
    var type: MoodType = MoodType.Neutral,
    var note: String = ""
)
