package org.wit.mood.models

enum class MoodType(val label: String, val score: Int) {
    Happy("Happy 😊", 2),
    Relaxed("Relaxed 😌", 1),
    Neutral("Neutral 😐", 0),
    Sad("Sad 😢", -1),
    Angry("Angry 😠", -2);
}

enum class SleepQuality { GOOD, MEDIUM, POOR }
enum class SocialActivity { FAMILY, FRIENDS, DATE, PARTY, NONE }
enum class Hobby { MOVIES, READING, GAMES, SPORT, RELAXATION, NONE }
enum class FoodType { HEALTHY, FASTFOOD, HOMEMADE, RESTAURANT, NOSUGAR, NONE }
