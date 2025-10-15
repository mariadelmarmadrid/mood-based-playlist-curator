package org.wit.mood.activities

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import org.wit.mood.R
import org.wit.mood.databinding.ActivityMoodBinding
import org.wit.mood.main.MainApp
import org.wit.mood.models.*
import timber.log.Timber.i
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MoodActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMoodBinding
    lateinit var app: MainApp

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMoodBinding.inflate(layoutInflater)
        setContentView(binding.root)

        app = application as MainApp
        i("Mood Activity started...")

        // --- Single-select behavior for the five chips ---
        val chips: List<Chip> = listOf(
            binding.chipHappy,
            binding.chipRelaxed,
            binding.chipNeutral,
            binding.chipSad,
            binding.chipAngry
        )
        chips.forEach { chip ->
            chip.setOnClickListener {
                chips.forEach { it.isChecked = it == chip }
            }
        }
        // Default selection
        binding.chipNeutral.isChecked = true

        // --- Spinners ---
        setupSpinner(binding.sleepQualitySpinner, R.array.sleep_quality)
        setupSpinner(binding.socialActivitySpinner, R.array.social_activity)
        setupSpinner(binding.hobbySpinner, R.array.hobbies)
        setupSpinner(binding.foodTypeSpinner, R.array.food_types)

        // --- Add button ---
        binding.btnAdd.setOnClickListener {
            val timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

            val selectedChip = chips.firstOrNull { it.isChecked }
            if (selectedChip == null) {
                Snackbar.make(binding.root, "Please select a mood!", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val moodLabel = (selectedChip.tag as? String).orEmpty()
            val moodType = MoodType.values().firstOrNull { it.label == moodLabel }
            if (moodType == null) {
                Snackbar.make(binding.root, "Invalid mood selected!", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Safely parse enums from spinners
            val sleep = enumOrDefault(
                binding.sleepQualitySpinner.selectedItem?.toString(),
                default = SleepQuality.MEDIUM
            ) { it.uppercase() }

            val social = enumOrDefault(
                binding.socialActivitySpinner.selectedItem?.toString(),
                default = SocialActivity.NONE
            ) { it.uppercase() }

            val hobby = enumOrDefault(
                binding.hobbySpinner.selectedItem?.toString(),
                default = Hobby.NONE
            ) { it.uppercase() }

            val food = enumOrDefault(
                binding.foodTypeSpinner.selectedItem?.toString(),
                default = FoodType.NONE
            ) { it.replace(" ", "_").uppercase() }

            val mood = MoodModel(
                type = moodType,
                note = binding.note.text?.toString().orEmpty(),
                sleep = sleep,
                social = social,
                hobby = hobby,
                food = food,
                timestamp = timestamp
            )

            app.moods.create(mood)
            i("Mood Added at $timestamp: $mood")
            Snackbar.make(binding.root, "Mood Added!", Snackbar.LENGTH_SHORT).show()
            setResult(RESULT_OK)
            finish()
        }

        // --- Cancel button ---
        binding.btnCancel.setOnClickListener {
            i("Mood creation canceled by user.")
            finish()
        }
    }

    private fun setupSpinner(spinner: Spinner, arrayRes: Int) {
        val adapter = ArrayAdapter.createFromResource(
            this, arrayRes, android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    private inline fun <reified E : Enum<E>> enumOrDefault(
        raw: String?,
        default: E,
        normalise: (String) -> String = { it }
    ): E {
        val text = raw?.trim().orEmpty()
        if (text.isEmpty()) return default
        return try {
            java.lang.Enum.valueOf(E::class.java, normalise(text))
        } catch (_: IllegalArgumentException) {
            default
        }
    }
}
