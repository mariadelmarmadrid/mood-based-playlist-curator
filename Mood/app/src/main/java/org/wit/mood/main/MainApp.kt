package org.wit.mood.main

import android.app.Application
import org.wit.mood.models.MoodJSONStore
import org.wit.mood.models.MoodStore
import timber.log.Timber
import timber.log.Timber.i

class MainApp : Application() {

    lateinit var moods: MoodStore

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        moods = MoodJSONStore(applicationContext)
        i("Mood App started")
    }
}
