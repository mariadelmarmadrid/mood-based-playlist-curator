package org.wit.mood.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import org.wit.mood.databinding.ActivityMoodBinding
import org.wit.mood.main.MainApp
import org.wit.mood.models.MoodModel
import org.wit.mood.models.MoodType
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
            val selectedMood = when (binding.moodGroup.checkedRadioButtonId) {
                binding.moodHappy.id -> MoodType.Happy
                binding.moodRelaxed.id -> MoodType.Relaxed
                binding.moodNeutral.id -> MoodType.Neutral
                binding.moodSad.id -> MoodType.Sad
                binding.moodAngry.id -> MoodType.Angry
                else -> null
            }

            if (selectedMood != null) {
                mood.type = selectedMood
                mood.note = binding.note.text.toString()
                app.moods.add(mood.copy())
                i("Mood Added: $mood")
                for (i in app.moods.indices) {
                    i("Mood[$i]: ${app.moods[i]}")
                }

                setResult(RESULT_OK)
                finish()
            } else {
                Snackbar.make(it, "Please select a mood", Snackbar.LENGTH_LONG).show()
            }
        }
    }
}
