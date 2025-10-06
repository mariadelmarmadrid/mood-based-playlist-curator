package org.wit.mood.main

import android.app.Application
import org.wit.mood.models.MoodModel
import timber.log.Timber
import timber.log.Timber.i

class MainApp : Application() {

    val moods = ArrayList<MoodModel>()

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        i("Mood App started")
    }
}
