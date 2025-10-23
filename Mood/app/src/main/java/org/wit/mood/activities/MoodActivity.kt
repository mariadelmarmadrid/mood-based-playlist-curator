package org.wit.mood.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
import org.wit.mood.R
import org.wit.mood.databinding.ActivityMoodBinding
import org.wit.mood.main.MainApp
import org.wit.mood.models.*
import timber.log.Timber.i
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Screen to CREATE or EDIT a mood entry.
 *
 * - If launched without extras → create mode.
 * - If launched with "mood_edit" Parcelable extra → edit mode (pre-fills UI, updates on save).
 *
 * Persists data via MainApp.moods (JSON store).
 */

class MoodActivity : AppCompatActivity() {

    // ViewBinding for this layout (type-safe access to views)
    private lateinit var binding: ActivityMoodBinding

    // Application-level reference exposing the MoodStore
    private lateinit var app: MainApp

    // Holds the mood being edited; null = create mode
    private var editingMood: MoodModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMoodBinding.inflate(layoutInflater)
        setContentView(binding.root)

        app = application as MainApp
        i("Mood Activity started…")

        // Make the 5 emoji chips behave like a SINGLE-SELECTION group
        wireSingleSelectChips(
            binding.chipHappy,
            binding.chipRelaxed,
            binding.chipNeutral,
            binding.chipSad,
            binding.chipAngry
        )

        // Default selection for brand-new entries
        binding.chipNeutral.isChecked = true

        // --- EDIT MODE: if a MoodModel was passed in, pre-fill the form and switch button text ---
        editingMood = intent.getParcelableExtra("mood_edit")
        editingMood?.let { m ->
            // Select the main mood chip that matches the existing entry
            when (m.type) {
                MoodType.HAPPY   -> binding.chipHappy.isChecked = true
                MoodType.RELAXED -> binding.chipRelaxed.isChecked = true
                MoodType.NEUTRAL -> binding.chipNeutral.isChecked = true
                MoodType.SAD     -> binding.chipSad.isChecked = true
                MoodType.ANGRY   -> binding.chipAngry.isChecked = true
            }

            // Pre-select OPTIONAL detail chips only if the value exists on the model
            selectChipByText(binding.sleepChipGroup,   m.sleep?.name?.lowercase()?.replaceFirstChar { it.uppercase() })
            selectChipByText(binding.socialChipGroup,  m.social?.name?.lowercase()?.replaceFirstChar { it.uppercase() })
            selectChipByText(binding.hobbyChipGroup,   m.hobby?.name?.lowercase()?.replaceFirstChar { it.uppercase() })
            selectChipByText(binding.foodChipGroup,    m.food?.name?.lowercase()?.replace('_',' ')?.replaceFirstChar { it.uppercase() })

            // Prefill note and swap the button label to "Update"
            binding.note.setText(m.note)
            binding.btnAdd.text = getString(R.string.update)
        }

        // Primary actions
        binding.btnAdd.setOnClickListener { onSaveClicked() }
        binding.btnCancel.setOnClickListener { finish() }
    }

    // ---------- Actions ----------
    /**
     * Validate inputs, then either CREATE a new mood or UPDATE the existing one.
     * Shows a brief Snackbar for feedback and finishes the Activity with RESULT_OK.
     */
    private fun onSaveClicked() {
        // Must have one main mood selected
        val selectedType = selectedMoodTypeOrNull()
        if (selectedType == null) {
            Snackbar.make(binding.root, "Please select a mood!", Snackbar.LENGTH_SHORT).show()
            return
        }

        // Read OPTIONAL details (null when nothing chosen)
        val sleep  = sleepFromChip(  selectedChipText(binding.sleepChipGroup))
        val social = socialFromChip( selectedChipText(binding.socialChipGroup))
        val hobby  = hobbyFromChip(  selectedChipText(binding.hobbyChipGroup))
        val food   = foodFromChip(   selectedChipText(binding.foodChipGroup))

        if (editingMood == null) {
            // --- CREATE path ---
            // Timestamp format used consistently across the app
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
            // --- UPDATE path ---
            // Keep the original id and timestamp to preserve history ordering
            val updated = editingMood!!.copy(
                type = selectedType,
                note = binding.note.text?.toString().orEmpty(),
                sleep = sleep,
                social = social,
                hobby = hobby,
                food = food,
                timestamp = editingMood!!.timestamp
            )
            app.moods.update(updated)
            i("Mood updated: $updated")
            Snackbar.make(binding.root, "Mood updated!", Snackbar.LENGTH_SHORT).show()
        }

        // Let the caller (e.g., list screen) know something changed
        setResult(RESULT_OK)
        finish()
    }

    // ---------- Helpers ----------

    /**
     * Makes a set of Chips act like a single-choice group.
     * Only one chip can be checked at a time.
     */
    private fun wireSingleSelectChips(vararg chips: Chip) {
        chips.forEach { chip ->
            chip.setOnCheckedChangeListener { button, isChecked ->
                if (isChecked) {
                    // Uncheck every other chip
                    chips.filter { it.id != button.id }.forEach { it.isChecked = false }
                }
            }
        }
    }

    /**
     * Returns the main MoodType based on which emoji chip is checked.
     * NOTE: This matches chip.tag (string) to MoodType.label; keep labels in sync.
     * Consider tagging chips with the enum directly for extra safety.
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

    // ---- Converters & small utilities ----

    /**
     * @return the displayed text of the selected Chip in a ChipGroup, or null if none selected.
     */
    private fun selectedChipText(group: ChipGroup): String? {
        val id = group.checkedChipId
        if (id == -1) return null
        val chip = group.findViewById<Chip>(id)
        return chip?.text?.toString()
    }

    // Map label text → enum value (null if no selection).
    // NOTE: depends on the Chip text exactly matching the enum name (with casing/spaces handled below).n
    private fun sleepFromChip(text: String?): SleepQuality? =
        text?.let { SleepQuality.valueOf(it.uppercase()) }

    private fun socialFromChip(text: String?): SocialActivity? =
        text?.let { SocialActivity.valueOf(it.uppercase()) }

    private fun hobbyFromChip(text: String?): Hobby? =
        text?.let { Hobby.valueOf(it.uppercase()) }

    private fun foodFromChip(text: String?): FoodType? =
        text?.let { FoodType.valueOf(it.replace(" ", "_").uppercase()) }

    /**
     * Pre-select a Chip in a group by its displayed text (case sensitive).
     * Used in edit mode to restore previous choices.
     * NOTE: This is string-based; if you localize, consider enum tags instead.
     */
    private fun selectChipByText(group: ChipGroup, text: String?) {
        if (text.isNullOrEmpty()) return
        for (i in 0 until group.childCount) {
            val chip = group.getChildAt(i) as? Chip ?: continue
            if (chip.text.toString() == text) {
                chip.isChecked = true
                return
            }
        }
    }
}
