package org.wit.mood.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import org.wit.mood.databinding.ActivityMoodBinding
import org.wit.mood.models.MoodModel
import timber.log.Timber
import timber.log.Timber.i

class MoodActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMoodBinding
    var mood = MoodModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMoodBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Timber.plant(Timber.DebugTree())
        i("Mood Activity started...")

        binding.btnAdd.setOnClickListener {
            mood.mood = binding.moodTitle.text.toString()
            if (mood.mood.isNotEmpty()) {
                i("Add Button Pressed: ${mood.mood}")
            } else {
                Snackbar.make(it, "Please enter a mood", Snackbar.LENGTH_LONG).show()
            }
        }
    }
}
