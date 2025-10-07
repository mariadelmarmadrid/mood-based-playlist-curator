package org.wit.mood.activities

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import org.wit.mood.databinding.ActivityMoodBinding
import org.wit.mood.main.MainApp
import org.wit.mood.models.*
import timber.log.Timber.i
import java.time.LocalDateTime
import org.wit.mood.R

class MoodActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMoodBinding
    lateinit var app: MainApp

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMoodBinding.inflate(layoutInflater)
        setContentView(binding.root)

        app = application as MainApp
        i("Mood Activity started...")

        // Setup spinners
        setupSpinner(binding.spinnerMoodType, R.array.mood_types)
        setupSpinner(binding.spinnerSleep, R.array.sleep_quality)
        setupSpinner(binding.spinnerSocial, R.array.social_activity)
        setupSpinner(binding.spinnerHobby, R.array.hobbies)
        setupSpinner(binding.spinnerFood, R.array.food_types)

        binding.btnAddMood.setOnClickListener {
            val mood = MoodModel(
                type = MoodType.valueOf(binding.spinnerMoodType.selectedItem.toString().uppercase()),
                note = binding.editNote.text.toString(),
                sleep = SleepQuality.valueOf(binding.spinnerSleep.selectedItem.toString().uppercase()),
                social = SocialActivity.valueOf(binding.spinnerSocial.selectedItem.toString().uppercase()),
                hobby = Hobby.valueOf(binding.spinnerHobby.selectedItem.toString().uppercase()),
                food = FoodType.valueOf(binding.spinnerFood.selectedItem.toString().uppercase()),
                timestamp = LocalDateTime.now().toString()
            )

            app.moods.add(mood)
            i("Mood Added: $mood")

            Snackbar.make(binding.root, "Mood Added!", Snackbar.LENGTH_SHORT).show()
            setResult(RESULT_OK)
            finish()
        }
    }

    private fun setupSpinner(spinner: android.widget.Spinner, arrayRes: Int) {
        ArrayAdapter.createFromResource(
            this,
            arrayRes,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }
    }
}
