package org.wit.mood.main

import android.app.Application
import org.wit.mood.models.MoodJSONStore
import org.wit.mood.models.MoodStore
import timber.log.Timber
import timber.log.Timber.i

/**
 * Main application class for the Mood app.
 *
 * Purpose:
 * - Initializes global singletons and libraries when the app starts.
 * - Provides a single shared instance of the MoodStore (our data layer)
 *   that can be accessed from any Activity.
 * - Configures Timber for logging.
 *
 * This class is declared in the AndroidManifest.xml as the app's
 * `android:name`, so its onCreate() runs before any Activity launches.
 */
class MainApp : Application() {

    // Shared data store accessible throughout the app
    lateinit var moods: MoodStore

    /** Called once when the app process is created. */
    override fun onCreate() {
        super.onCreate()

        // Initialize the Timber logging library (debug tree for development)
        Timber.plant(Timber.DebugTree())

        // Initialize our mood data store using JSON persistence
        moods = MoodJSONStore(applicationContext)

        // Log that the app has started successfully
        i("Mood App started")
    }
}
