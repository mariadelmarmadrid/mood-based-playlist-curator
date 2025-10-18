package org.wit.mood.models

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import timber.log.Timber
import java.io.File

private const val JSON_FILE = "moods.json"
private val gsonBuilder: Gson = GsonBuilder().setPrettyPrinting().create()
private val listType = object : TypeToken<MutableList<MoodModel>>() {}.type

class MoodJSONStore(private val context: Context) : MoodStore {

    private var moods = mutableListOf<MoodModel>()
    private var lastId = 0L

    init {
        if (File(context.filesDir, JSON_FILE).exists()) {
            deserialize()
            // Backfill missing IDs if loading legacy data
            var changed = false
            moods.forEach { mood ->
                if (mood.id == 0L) { mood.id = nextId(); changed = true }
                else if (mood.id > lastId) { lastId = mood.id }
            }
            if (changed) serialize()
        }
    }

    private fun nextId(): Long = ++lastId

    override fun findAll(): List<MoodModel> = moods

    override fun findById(id: Long): MoodModel? = moods.firstOrNull { it.id == id }

    override fun create(mood: MoodModel) {
        if (mood.id == 0L) mood.id = nextId()
        moods.add(mood)
        serialize()
    }

    override fun update(mood: MoodModel) {
        val index = moods.indexOfFirst { it.id == mood.id }
        if (index >= 0) {
            moods[index] = mood
            serialize()
        }
    }

    override fun delete(mood: MoodModel) {
        moods.removeAll { it.id == mood.id }
        serialize()
    }


    private fun serialize() {
        val jsonString = gsonBuilder.toJson(moods, listType)
        context.openFileOutput(JSON_FILE, Context.MODE_PRIVATE).use {
            it.write(jsonString.toByteArray())
        }
        Timber.i("Saved ${moods.size} moods to JSON.")
    }

    private fun deserialize() {
        val jsonString = context.openFileInput(JSON_FILE).bufferedReader().use { it.readText() }
        moods = gsonBuilder.fromJson(jsonString, listType)
        Timber.i("Loaded ${moods.size} moods from JSON.")
    }
}
