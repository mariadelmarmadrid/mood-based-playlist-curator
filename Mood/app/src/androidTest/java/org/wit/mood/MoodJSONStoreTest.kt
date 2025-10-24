package org.wit.mood

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.wit.mood.models.*
import java.io.File

/**
 * Instrumented tests for MoodJSONStore.
 *
 * Goals:
 *  - Verify CRUD works in-memory and persists to disk (moods.json).
 *  - Ensure IDs are stable across process boundaries (re-initialization).
 *  - Prove delete persists too (item is gone after reloading).
 *
 */
@RunWith(AndroidJUnit4::class)
class MoodJSONStoreTest {

    private lateinit var context: Context
    private lateinit var store: MoodJSONStore
    private lateinit var jsonFile: File

    @Before
    fun setup() {
        // Obtain the target app context for file I/O under filesDir.
        context = ApplicationProvider.getApplicationContext()
        // Point to the exact JSON file the store uses internally.
        jsonFile = File(context.filesDir, "moods.json")
        // Test isolation: start each test from a clean slate
        if (jsonFile.exists()) jsonFile.delete()
        // Create a fresh store (will create the file on first serialize()).
        store = MoodJSONStore(context)
    }

    @After
    fun tearDown() {
        // Cleanup ensures the device/emulator has no leftover state
        // that could affect later test runs.
        if (jsonFile.exists()) jsonFile.delete()
    }

    @Test
    fun create_and_persist_moods() {
        // Initially empty (new file / clean slate).
        assertTrue(store.findAll().isEmpty())

        // Create two distinct moods with fixed timestamps for determinism.
        val m1 = MoodModel(type = MoodType.HAPPY, note = "Sunshine", timestamp = "2025-10-19 09:12:03")
        val m2 = MoodModel(type = MoodType.SAD,   note = "Lost my keys", timestamp = "2025-10-19 21:05:41")

        // Persist them (should write to disk as part of create()).
        store.create(m1)
        store.create(m2)

        // In-memory sanity check.
        val all = store.findAll()
        assertEquals(2, all.size)
        assertTrue(all.any { it.note == "Sunshine" })
        assertTrue(all.any { it.note == "Lost my keys" })

        // Recreate the store to force a fresh read from disk (deserialize()).
        val reloaded = MoodJSONStore(context)
        val again = reloaded.findAll()

        // Data should round-trip via JSON unchanged.
        assertEquals(2, again.size)
        assertTrue(again.any { it.note == "Sunshine" })
        assertTrue(again.any { it.note == "Lost my keys" })
    }

    @Test
    fun update_and_findById() {
        // Create one mood.
        val m = MoodModel(type = MoodType.NEUTRAL, note = "ok", timestamp = "2025-10-20 10:00:00")
        store.create(m)

        // Grab it as created (to get the generated id).
        val created = store.findAll().first()
        // Mutate fields but keep the same id (copy()).
        val updated = created.copy(note = "actually good", type = MoodType.RELAXED)

        // Persist the update (should overwrite the entry with same id).
        store.update(updated)

        // Verify we can retrieve by id and the fields changed as expected.
        val byId = store.findById(updated.id)
        assertNotNull(byId)
        assertEquals("actually good", byId!!.note)
        assertEquals(MoodType.RELAXED, byId.type)
    }

    @Test
    fun delete_and_reload() {
        // Insert two entries.
        val m1 = MoodModel(type =  MoodType.ANGRY, note = "ugh", timestamp = "2025-10-21 08:00:00")
        val m2 = MoodModel(type = MoodType.RELAXED, note = "spa", timestamp = "2025-10-21 12:00:00")
        store.create(m1)
        store.create(m2)

        // Delete one by id.
        val toDelete = store.findAll().first()
        store.delete(toDelete)

        // In-memory check that size dropped and id is gone.
        assertEquals(1, store.findAll().size)
        assertFalse(store.findAll().any { it.id == toDelete.id })

        // Recreate store to enforce disk read: deleted item must remain deleted.
        val reloaded = MoodJSONStore(context)
        assertEquals(1, reloaded.findAll().size)
        assertFalse(reloaded.findAll().any { it.id == toDelete.id })
    }
}
