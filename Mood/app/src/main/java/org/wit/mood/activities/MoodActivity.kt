package org.wit.mood.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import org.wit.mood.databinding.ActivityMoodBinding
import org.wit.mood.main.MainApp
import org.wit.mood.models.MoodModel
import timber.log.Timber.i

class MoodActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMoodBinding
    var mood = MoodModel()
    lateinit var app: MainApp

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMoodBinding.inflate(layoutInflater)
        setContentView(binding.root)

        app = application as MainApp

        i("Mood Activity started...")

        binding.btnAdd.setOnClickListener {
            mood.mood = binding.moodTitle.text.toString()
            mood.note = binding.note.text.toString()
            if (mood.mood.isNotEmpty()) {
                app.moods.add(mood.copy())
                i("Add Button Pressed: $mood")
                for (i in app.moods.indices) {
                    i("Mood[$i]: ${this.app.moods[i]}")
                }
                setResult(RESULT_OK)
                finish()
            } else {
                Snackbar.make(it, "Please enter your mood", Snackbar.LENGTH_LONG).show()
            }
        }
    }
}
