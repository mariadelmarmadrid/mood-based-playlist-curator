package org.wit.mood.models

/**
 * Represents the main emotional state selected by the user.
 *
 * Each mood type has:
 *  - a user-friendly label (with emoji) for UI display
 *  - a numeric score used in calculations (e.g., daily averages)
 *
 * Positive moods have positive scores, negative moods have negative scores.
 */
enum class MoodType(val label: String, val score: Int) {
    HAPPY("Happy 😊", 2),
    RELAXED("Relaxed 😌", 1),
    NEUTRAL("Neutral 😐", 0),
    SAD("Sad 😢", -1),
    ANGRY("Angry 😠", -2);
}

/**
 * Describes how well the user slept the night before.
 * Used as an optional contextual detail for a mood entry.
 */
enum class SleepQuality { GOOD, MEDIUM, POOR }

/**
 * Represents the user’s social context or interaction type for that day.
 */
enum class SocialActivity { FAMILY, FRIENDS, DATE, PARTY }

/**
 * Describes the user’s main hobby or leisure activity of the day.
 */
enum class Hobby { MOVIES, READING, GAMES, SPORT, RELAXATION }

/**
 * Represents the primary type of food the user consumed that day.
 */
enum class FoodType { HEALTHY, FAST_FOOD, HOMEMADE, RESTAURANT, NO_SUGAR }
