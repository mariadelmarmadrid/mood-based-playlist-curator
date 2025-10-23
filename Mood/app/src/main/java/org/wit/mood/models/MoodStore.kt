package org.wit.mood.models


/**
 * Defines the contract for a Mood data store (Repository layer).
 *
 * Any class that implements this interface (e.g. [MoodJSONStore])
 * must provide basic CRUD functionality for [MoodModel] objects.
 *
 * This abstraction allows you to:
 * - Swap storage implementations easily (e.g., JSON → Room DB)
 * - Keep Activities independent from data storage details
 * - Follow clean architecture principles (separation of concerns)
 */
interface MoodStore {
    /** Retrieve all saved moods from storage. */
    fun findAll(): List<MoodModel>

    /**
     * Find a single mood by its unique ID.
     * @param id The ID of the mood to look for.
     * @return The matching MoodModel, or null if not found.
     */
    fun findById(id: Long): MoodModel?

    /**
     * Add a new mood entry to storage.
     * Implementations should assign a unique ID and persist it.
     */
    fun create(mood: MoodModel)

    /**
     * Update an existing mood (matched by ID).
     * Fields from the provided object replace the stored version.
     */
    fun update(mood: MoodModel)

    /**
     * Delete a mood entry from storage.
     * The item is removed permanently from the data source.
     */
    fun delete(mood: MoodModel)
}
