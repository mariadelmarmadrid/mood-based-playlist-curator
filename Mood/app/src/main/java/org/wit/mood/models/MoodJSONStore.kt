package org.wit.mood.models

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import timber.log.Timber
import java.io.File

// --- File + JSON setup ---
private const val JSON_FILE = "moods.json"  // File name for local JSON storage
private val gsonBuilder: Gson = GsonBuilder().setPrettyPrinting().create()
private val listType = object : TypeToken<MutableList<MoodModel>>() {}.type

/**
 * Concrete implementation of [MoodStore] that persists data in a local JSON file.
 *
 * Responsibilities:
 * - Save all moods to a JSON file in the app's private storage.
 * - Load existing moods on startup.
 * - Manage unique IDs for new moods.
 * - Provide CRUD operations (create, read, update, delete).
 *
 * Data is stored at:
 *   /data/data/org.wit.mood/files/moods.json
 */
class MoodJSONStore(private val context: Context) : MoodStore {

    // In-memory cache of all moods loaded from JSON
    private var moods = mutableListOf<MoodModel>()

    // Tracks the last used ID to ensure unique IDs for new entries
    private var lastId = 0L

    /**
     * Initialization block runs when the store is created.
     * Loads existing data from JSON if the file exists.
     * Also assigns IDs to any older entries that might be missing one.
     */
    init {
        if (File(context.filesDir, JSON_FILE).exists()) {
            deserialize()

            // Backfill missing IDs in legacy data (from older app versions)
            var changed = false
            moods.forEach { mood ->
                if (mood.id == 0L) { mood.id = nextId(); changed = true } // assign new unique ID
                else if (mood.id > lastId) { lastId = mood.id } // keep track of highest ID seen
            }
            if (changed) serialize()
        }
    }

    /** Generate a new unique ID for a MoodModel. */
    private fun nextId(): Long = ++lastId

    // --- CRUD operations (defined by MoodStore interface) ---

    /** Return all moods currently stored in memory. */
    override fun findAll(): List<MoodModel> = moods

    /** Find one mood by its unique ID (or null if not found). */
    override fun findById(id: Long): MoodModel? = moods.firstOrNull { it.id == id }

    /**
     * Add a new mood to the store.
     * Assigns an ID if needed, appends it to the list, and re-saves to JSON.
     */
    override fun create(mood: MoodModel) {
        if (mood.id == 0L) mood.id = nextId()
        moods.add(mood)
        serialize()
    }

    /**
     * Update an existing mood (match by ID).
     * Replaces the old version in the list and re-saves to JSON.
     */
    override fun update(mood: MoodModel) {
        val index = moods.indexOfFirst { it.id == mood.id }
        if (index >= 0) {
            moods[index] = mood
            serialize()
        }
    }

    /**
     * Delete a mood from the store (match by ID) and re-save to JSON.
     */
    override fun delete(mood: MoodModel) {
        moods.removeAll { it.id == mood.id }
        serialize()
    }

    // --- JSON serialization helpers ---

    /**
     * Converts the current mood list to JSON and writes it to internal storage.
     * Uses Gson for conversion and Timber for debug logging.
     */
    private fun serialize() {
        val jsonString = gsonBuilder.toJson(moods, listType)
        context.openFileOutput(JSON_FILE, Context.MODE_PRIVATE).use {
            it.write(jsonString.toByteArray())
        }
        Timber.i("Saved ${moods.size} moods to JSON.")
    }

    /**
     * Reads the JSON file from internal storage and loads it into memory.
     */
    private fun deserialize() {
        val jsonString = context.openFileInput(JSON_FILE).bufferedReader().use { it.readText() }
        moods = gsonBuilder.fromJson(jsonString, listType)
        Timber.i("Loaded ${moods.size} moods from JSON.")
    }
}
