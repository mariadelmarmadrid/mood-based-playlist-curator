package org.wit.mood.activities

import android.os.Bundle
import android.widget.ArrayAdapter
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
    private lateinit var app: MainApp

    // When not null, we are editing an existing mood
    private var editingMood: MoodModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMoodBinding.inflate(layoutInflater)
        setContentView(binding.root)

        app = application as MainApp
        i("Mood Activity started…")

        // Spinners
        setupSpinner(binding.sleepQualitySpinner, R.array.sleep_quality)
        setupSpinner(binding.socialActivitySpinner, R.array.social_activity)
        setupSpinner(binding.hobbySpinner, R.array.hobbies)
        setupSpinner(binding.foodTypeSpinner, R.array.food_types)

        // Wire single-selection behavior for standalone Chips (no ChipGroup in this layout)
        wireSingleSelectChips(
            binding.chipHappy,
            binding.chipRelaxed,
            binding.chipNeutral,
            binding.chipSad,
            binding.chipAngry
        )

        // Default selection (Neutral) for new entries
        binding.chipNeutral.isChecked = true

        // EDIT MODE: if a mood was passed in, prefill UI and change button text
        editingMood = intent.getParcelableExtra("mood_edit")
        editingMood?.let { m ->
            when (m.type) {
                MoodType.HAPPY   -> binding.chipHappy.isChecked = true
                MoodType.RELAXED -> binding.chipRelaxed.isChecked = true
                MoodType.NEUTRAL -> binding.chipNeutral.isChecked = true
                MoodType.SAD     -> binding.chipSad.isChecked = true
                MoodType.ANGRY   -> binding.chipAngry.isChecked = true
            }

            binding.note.setText(m.note)
            binding.sleepQualitySpinner.setSelection(m.sleep.ordinal)
            binding.socialActivitySpinner.setSelection(m.social.ordinal)
            binding.hobbySpinner.setSelection(m.hobby.ordinal)
            binding.foodTypeSpinner.setSelection(m.food.ordinal)

            binding.btnAdd.text = getString(R.string.update) // add "Update Mood" in strings.xml if you prefer
        }

        binding.btnAdd.setOnClickListener { onSaveClicked() }
        binding.btnCancel.setOnClickListener { finish() }
    }

    // ---------- UI Actions ----------

    private fun onSaveClicked() {
        val selectedType = selectedMoodTypeOrNull()
        if (selectedType == null) {
            Snackbar.make(binding.root, "Please select a mood!", Snackbar.LENGTH_SHORT).show()
            return
        }

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

        if (editingMood == null) {
            // CREATE new mood
            val timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

            val newMood = MoodModel(
                type = selectedType,
                note = binding.note.text?.toString().orEmpty(),
                sleep = sleep,
                social = social,
                hobby = hobby,
                food = food,
                timestamp = timestamp
            )
            app.moods.create(newMood)
            i("Mood created: $newMood")
            Snackbar.make(binding.root, "Mood added!", Snackbar.LENGTH_SHORT).show()
        } else {
            // UPDATE existing mood (preserve id; keep timestamp as-is or change if you want)
            val updated = editingMood!!.copy(
                type = selectedType,
                note = binding.note.text?.toString().orEmpty(),
                sleep = sleep,
                social = social,
                hobby = hobby,
                food = food
                // timestamp = editingMood!!.timestamp // keep original timestamp; uncomment if you want to force keep
            )
            app.moods.update(updated)
            i("Mood updated: $updated")
            Snackbar.make(binding.root, "Mood updated!", Snackbar.LENGTH_SHORT).show()
        }

        setResult(RESULT_OK)
        finish()
    }

    // ---------- Helpers ----------

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

    /**
     * Make a group of Chips behave like a single-selection group (since we don’t have a ChipGroup).
     */
    private fun wireSingleSelectChips(vararg chips: Chip) {
        chips.forEach { chip ->
            chip.setOnCheckedChangeListener { button, isChecked ->
                if (isChecked) {
                    chips.filter { it.id != button.id }.forEach { it.isChecked = false }
                }
            }
        }
    }

    /**
     * Determine the selected mood type by reading the tag of the checked chip.
     * Each chip in your layout has android:tag like "Happy 😊", "Relaxed 😌", etc.
     * MoodType.label should match those exact strings.
     */
    private fun selectedMoodTypeOrNull(): MoodType? {
        val checkedChip = listOf(
            binding.chipHappy,
            binding.chipRelaxed,
            binding.chipNeutral,
            binding.chipSad,
            binding.chipAngry
        ).firstOrNull { it.isChecked } ?: return null

        val labelFromTag = (checkedChip.tag as? String).orEmpty()
        return MoodType.values().firstOrNull { it.label == labelFromTag }
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
