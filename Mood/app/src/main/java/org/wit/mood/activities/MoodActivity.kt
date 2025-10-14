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
import java.time.format.DateTimeFormatter
import android.widget.RadioButton


class MoodActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMoodBinding
    lateinit var app: MainApp

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMoodBinding.inflate(layoutInflater)
        setContentView(binding.root)

        app = application as MainApp
        i("Mood Activity started...")

        setupSpinner(binding.sleepQualitySpinner, R.array.sleep_quality)
        setupSpinner(binding.socialActivitySpinner, R.array.social_activity)
        setupSpinner(binding.hobbySpinner, R.array.hobbies)
        setupSpinner(binding.foodTypeSpinner, R.array.food_types)

        binding.btnAdd.setOnClickListener {
            val current = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            val timestamp = current.format(formatter)

            val selectedMoodId = binding.moodRadioGroup.checkedRadioButtonId
            if (selectedMoodId == -1) {
                Snackbar.make(binding.root, "Please select a mood!", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedMoodButton = findViewById<RadioButton>(selectedMoodId)
            val moodType = MoodType.values().firstOrNull { it.label == selectedMoodButton.text.toString() }
            if (moodType == null) {
                Snackbar.make(binding.root, "Invalid mood selected!", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val mood = MoodModel(
                type = moodType,
                note = binding.note.text.toString(),
                sleep = SleepQuality.valueOf(binding.sleepQualitySpinner.selectedItem.toString().uppercase()),
                social = SocialActivity.valueOf(binding.socialActivitySpinner.selectedItem.toString().uppercase()),
                hobby = Hobby.valueOf(binding.hobbySpinner.selectedItem.toString().uppercase()),
                food = FoodType.valueOf(binding.foodTypeSpinner.selectedItem.toString().replace(" ", "_").uppercase()),
                timestamp = timestamp
            )

            app.moods.create(mood)
            i("Mood Added at $timestamp: $mood")
            app.moods.findAll().forEachIndexed { index, m ->
                i("Mood[$index]: $m")
            }

            Snackbar.make(binding.root, "Mood Added!", Snackbar.LENGTH_SHORT).show()
            setResult(RESULT_OK)
            finish()
        }

        binding.btnCancel.setOnClickListener {
            i("Mood creation canceled by user.")
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
