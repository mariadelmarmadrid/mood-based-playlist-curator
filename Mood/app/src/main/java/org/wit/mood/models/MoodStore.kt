package org.wit.mood.models

interface MoodStore {
    fun findAll(): List<MoodModel>
    fun findById(id: Long): MoodModel?
    fun create(mood: MoodModel)
    fun update(mood: MoodModel)
    fun delete(mood: MoodModel)
}
