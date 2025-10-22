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

@RunWith(AndroidJUnit4::class)
class MoodJSONStoreTest {

    private lateinit var context: Context
    private lateinit var store: MoodJSONStore
    private lateinit var jsonFile: File

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        // Point to the same file your store uses internally
        jsonFile = File(context.filesDir, "moods.json")
        // Start with a clean slate for each test
        if (jsonFile.exists()) jsonFile.delete()
        store = MoodJSONStore(context) // uses Context file I/O internally (see your implementation)
    }

    @After
    fun tearDown() {
        // Clean up after each test
        if (jsonFile.exists()) jsonFile.delete()
    }

    @Test
    fun create_and_persist_moods() {
        assertTrue(store.findAll().isEmpty())

        val m1 = MoodModel(type = MoodType.HAPPY, note = "Sunshine", timestamp = "2025-10-19 09:12:03")
        val m2 = MoodModel(type = MoodType.SAD,   note = "Lost my keys", timestamp = "2025-10-19 21:05:41")

        store.create(m1)
        store.create(m2)

        // Sanity checks in-memory
        val all = store.findAll()
        assertEquals(2, all.size)
        assertTrue(all.any { it.note == "Sunshine" })
        assertTrue(all.any { it.note == "Lost my keys" })

        // Recreate store to force a disk read (deserialize)
        val reloaded = MoodJSONStore(context)
        val again = reloaded.findAll()
        assertEquals(2, again.size)
        assertTrue(again.any { it.note == "Sunshine" })
        assertTrue(again.any { it.note == "Lost my keys" })
    }

    @Test
    fun update_and_findById() {
        val m = MoodModel(type = MoodType.NEUTRAL, note = "ok", timestamp = "2025-10-20 10:00:00")
        store.create(m)

        val created = store.findAll().first()
        val updated = created.copy(note = "actually good", type = MoodType.RELAXED)

        store.update(updated)

        val byId = store.findById(updated.id)
        assertNotNull(byId)
        assertEquals("actually good", byId!!.note)
        assertEquals(MoodType.RELAXED, byId.type)
    }

    @Test
    fun delete_and_reload() {
        val m1 = MoodModel(type =  MoodType.ANGRY, note = "ugh", timestamp = "2025-10-21 08:00:00")
        val m2 = MoodModel(type = MoodType.RELAXED, note = "spa", timestamp = "2025-10-21 12:00:00")
        store.create(m1)
        store.create(m2)

        val toDelete = store.findAll().first()
        store.delete(toDelete)

        assertEquals(1, store.findAll().size)
        assertFalse(store.findAll().any { it.id == toDelete.id })

        // Reload from disk to verify persistence after delete
        val reloaded = MoodJSONStore(context)
        assertEquals(1, reloaded.findAll().size)
        assertFalse(reloaded.findAll().any { it.id == toDelete.id })
    }
}
