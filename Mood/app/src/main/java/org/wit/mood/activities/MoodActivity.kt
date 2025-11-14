package org.wit.mood.activities

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import coil.load
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

    // Image state (optional per note)
    private var selectedPhotoUri: Uri? = null

    // Modern Photo Picker (no storage permissions needed)
    private val pickImage = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedPhotoUri = uri
            showPhoto(uri)
        }
    }

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

        // --- EDIT MODE: pre-fill the form and swap button label to "Update" ---
        editingMood = intent.getParcelableExtra("mood_edit")
        editingMood?.let { m ->
            when (m.type) {
                MoodType.HAPPY   -> binding.chipHappy.isChecked = true
                MoodType.RELAXED -> binding.chipRelaxed.isChecked = true
                MoodType.NEUTRAL -> binding.chipNeutral.isChecked = true
                MoodType.SAD     -> binding.chipSad.isChecked = true
                MoodType.ANGRY   -> binding.chipAngry.isChecked = true
            }

            // Pre-select OPTIONAL detail chips
            selectChipByText(binding.sleepChipGroup,  m.sleep?.name?.lowercase()?.replaceFirstChar { it.uppercase() })
            selectChipByText(binding.socialChipGroup, m.social?.name?.lowercase()?.replaceFirstChar { it.uppercase() })
            selectChipByText(binding.hobbyChipGroup,  m.hobby?.name?.lowercase()?.replaceFirstChar { it.uppercase() })
            selectChipByText(binding.foodChipGroup,   m.food?.name?.lowercase()?.replace('_',' ')?.replaceFirstChar { it.uppercase() })

            // Prefill note and title the button "Update"
            binding.note.setText(m.note)
            binding.btnAdd.text = getString(R.string.update)
        }

        // Primary actions
        binding.btnAdd.setOnClickListener { onSaveClicked() }
        binding.btnCancel.setOnClickListener { finish() }

        // Photo actions
        binding.btnAddPhoto.setOnClickListener {
            pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        binding.btnRemovePhoto.setOnClickListener {
            selectedPhotoUri = null
            binding.photoPreview.visibility = View.GONE
            binding.btnRemovePhoto.visibility = View.GONE
            binding.btnAddPhoto.text = getString(R.string.button_add_photo) // Reset
        }


        // If editing and there is an existing photo, show it
        editingMood?.photoUri?.let {
            selectedPhotoUri = Uri.parse(it)
            showPhoto(selectedPhotoUri!!)
        } ?: run {
            // No photo loaded yet
            binding.btnAddPhoto.text = getString(R.string.button_add_photo)
        }

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
            val timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

            val newMood = MoodModel(
                type = selectedType,
                note = binding.note.text?.toString().orEmpty(),
                sleep = sleep,
                social = social,
                hobby = hobby,
                food = food,
                timestamp = timestamp,
                photoUri = selectedPhotoUri?.toString() // ✅ SAVE image on create
            )
            app.moods.create(newMood)
            i("Mood created: $newMood")
            Snackbar.make(binding.root, "Mood added!", Snackbar.LENGTH_SHORT).show()
        } else {
            // --- UPDATE path ---
            val updated = editingMood!!.copy(
                type = selectedType,
                note = binding.note.text?.toString().orEmpty(),
                sleep = sleep,
                social = social,
                hobby = hobby,
                food = food,
                timestamp = editingMood!!.timestamp, // keep original ordering
                photoUri = selectedPhotoUri?.toString() // ✅ SAVE (or clear) on update
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

    /** Makes a set of Chips act like a single-choice group. */
    private fun wireSingleSelectChips(vararg chips: Chip) {
        chips.forEach { chip ->
            chip.setOnCheckedChangeListener { button, isChecked ->
                if (isChecked) {
                    chips.filter { it.id != button.id }.forEach { it.isChecked = false }
                }
            }
        }
    }

    /** Returns the main MoodType based on which emoji chip is checked. */
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

    /** Selected chip text in a group (or null). */
    private fun selectedChipText(group: ChipGroup): String? {
        val id = group.checkedChipId
        if (id == -1) return null
        val chip = group.findViewById<Chip>(id)
        return chip?.text?.toString()
    }

    // Map label text → enum value (null if no selection).
    private fun sleepFromChip(text: String?): SleepQuality? =
        text?.let { SleepQuality.valueOf(it.uppercase()) }

    private fun socialFromChip(text: String?): SocialActivity? =
        text?.let { SocialActivity.valueOf(it.uppercase()) }

    private fun hobbyFromChip(text: String?): Hobby? =
        text?.let { Hobby.valueOf(it.uppercase()) }

    private fun foodFromChip(text: String?): FoodType? =
        text?.let { FoodType.valueOf(it.replace(" ", "_").uppercase()) }

    /** Pre-select a Chip in a group by its displayed text (case sensitive). */
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

    /** Show the picked photo in the preview area. */
    private fun showPhoto(uri: Uri) {
        binding.photoPreview.load(uri)
        binding.photoPreview.visibility = View.VISIBLE

        // When a photo exists:
        binding.btnAddPhoto.text = getString(R.string.button_change_photo)
        binding.btnRemovePhoto.visibility = View.VISIBLE
    }

}
