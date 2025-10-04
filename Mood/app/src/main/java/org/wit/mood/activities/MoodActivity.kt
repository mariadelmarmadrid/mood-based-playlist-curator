package org.wit.mood.activities

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.github.ajalt.timberkt.Timber
import org.wit.mood.R
import timber.log.Timber.i

class MoodActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mood)

        Timber.plant(Timber.DebugTree())
        i("Placemark Activity started..")
    }
}