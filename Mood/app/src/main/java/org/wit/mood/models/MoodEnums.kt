package org.wit.mood.models

enum class MoodType(val label: String, val score: Int) {
    HAPPY("Happy 😊", 2),
    RELAXED("Relaxed 😌", 1),
    NEUTRAL("Neutral 😐", 0),
    SAD("Sad 😢", -1),
    ANGRY("Angry 😠", -2);
}


enum class SleepQuality { GOOD, MEDIUM, POOR }
enum class SocialActivity { FAMILY, FRIENDS, DATE, PARTY, NONE }
enum class Hobby { MOVIES, READING, GAMES, SPORT, RELAXATION, NONE }
enum class FoodType {
    HEALTHY,
    FAST_FOOD,
    HOMEMADE,
    RESTAURANT,
    NO_SUGAR,
    NONE
}

