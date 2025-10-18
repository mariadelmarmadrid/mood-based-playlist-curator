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

class MoodActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMoodBinding
    private lateinit var app: MainApp

    // If not null, we're editing
    private var editingMood: MoodModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMoodBinding.inflate(layoutInflater)
        setContentView(binding.root)

        app = application as MainApp
        i("Mood Activity started…")

        // Make the 5 emoji chips behave like a single-selection group
        wireSingleSelectChips(
            binding.chipHappy,
            binding.chipRelaxed,
            binding.chipNeutral,
            binding.chipSad,
            binding.chipAngry
        )

        // Default: Neutral for new entries
        binding.chipNeutral.isChecked = true

        // --- EDIT MODE ---
        editingMood = intent.getParcelableExtra("mood_edit")
        editingMood?.let { m ->
            // Select main mood chip
            when (m.type) {
                MoodType.HAPPY   -> binding.chipHappy.isChecked = true
                MoodType.RELAXED -> binding.chipRelaxed.isChecked = true
                MoodType.NEUTRAL -> binding.chipNeutral.isChecked = true
                MoodType.SAD     -> binding.chipSad.isChecked = true
                MoodType.ANGRY   -> binding.chipAngry.isChecked = true
            }

            // Prefill optional detail groups (only if value present)
            selectChipByText(binding.sleepChipGroup,   m.sleep?.name?.lowercase()?.replaceFirstChar { it.uppercase() })
            selectChipByText(binding.socialChipGroup,  m.social?.name?.lowercase()?.replaceFirstChar { it.uppercase() })
            selectChipByText(binding.hobbyChipGroup,   m.hobby?.name?.lowercase()?.replaceFirstChar { it.uppercase() })
            selectChipByText(binding.foodChipGroup,    m.food?.name?.lowercase()?.replace('_',' ')?.replaceFirstChar { it.uppercase() })

            binding.note.setText(m.note)
            binding.btnAdd.text = getString(R.string.update)
        }

        binding.btnAdd.setOnClickListener { onSaveClicked() }
        binding.btnCancel.setOnClickListener { finish() }
    }

    // ---------- Actions ----------

    private fun onSaveClicked() {
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
            // CREATE
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
            // UPDATE (preserve id + timestamp)
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

        setResult(RESULT_OK)
        finish()
    }

    // ---------- Helpers ----------

    /** Make a set of Chips behave like single-selection (since emoji chips are not in a ChipGroup). */
    private fun wireSingleSelectChips(vararg chips: Chip) {
        chips.forEach { chip ->
            chip.setOnCheckedChangeListener { button, isChecked ->
                if (isChecked) {
                    chips.filter { it.id != button.id }.forEach { it.isChecked = false }
                }
            }
        }
    }

    /** Returns the selected mood type based on the checked emoji chip’s tag/label. */
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

    // ---- Your helpers: put them here (inside the Activity) ----

    private fun selectedChipText(group: ChipGroup): String? {
        val id = group.checkedChipId
        if (id == -1) return null
        val chip = group.findViewById<Chip>(id)
        return chip?.text?.toString()
    }

    // mappers that return null if no selection
    private fun sleepFromChip(text: String?): SleepQuality? =
        text?.let { SleepQuality.valueOf(it.uppercase()) }

    private fun socialFromChip(text: String?): SocialActivity? =
        text?.let { SocialActivity.valueOf(it.uppercase()) }

    private fun hobbyFromChip(text: String?): Hobby? =
        text?.let { Hobby.valueOf(it.uppercase()) }

    private fun foodFromChip(text: String?): FoodType? =
        text?.let { FoodType.valueOf(it.replace(" ", "_").uppercase()) }

    /** Helper to pre-select a chip in a group by its displayed text (case sensitive). */
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
