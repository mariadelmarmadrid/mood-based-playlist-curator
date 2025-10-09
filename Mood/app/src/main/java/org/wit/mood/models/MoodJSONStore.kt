package org.wit.mood.models

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import timber.log.Timber
import java.io.File

const val JSON_FILE = "moods.json"
val gsonBuilder: Gson = GsonBuilder().setPrettyPrinting().create()
val listType = object : TypeToken<MutableList<MoodModel>>() {}.type

class MoodJSONStore(private val context: Context) {

    private var moods = mutableListOf<MoodModel>()

    init {
        if (File(context.filesDir, JSON_FILE).exists()) {
            deserialize()
        }
    }

    fun findAll(): List<MoodModel> = moods

    fun create(mood: MoodModel) {
        moods.add(mood)
        serialize()
    }

    fun delete(mood: MoodModel) {
        moods.remove(mood)
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
