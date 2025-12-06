package org.wit.mood.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
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

class MoodActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMoodBinding
    private lateinit var app: MainApp

    private var editingMood: MoodModel? = null
    private var selectedPhotoUri: Uri? = null

    // Your own Location data class (org.wit.mood.models.Location)
    private var currentLocation: Location? = null

    private lateinit var mapIntentLauncher: ActivityResultLauncher<Intent>

    // Modern Photo Picker
    private val pickImage = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
            try {
                contentResolver.takePersistableUriPermission(uri, flag)
            } catch (_: SecurityException) { }
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

        // Register callback for map result
        registerMapCallback()

        // --- Mood emoji chips behave like single-select ---
        wireSingleSelectChips(
            binding.chipHappy,
            binding.chipRelaxed,
            binding.chipNeutral,
            binding.chipSad,
            binding.chipAngry
        )
        binding.chipNeutral.isChecked = true

        // --- EDIT MODE: if mood_edit extra exists, prefill form ---
        editingMood = intent.getParcelableExtra("mood_edit")
        editingMood?.let { m ->
            // Main mood
            when (m.type) {
                MoodType.HAPPY   -> binding.chipHappy.isChecked = true
                MoodType.RELAXED -> binding.chipRelaxed.isChecked = true
                MoodType.NEUTRAL -> binding.chipNeutral.isChecked = true
                MoodType.SAD     -> binding.chipSad.isChecked = true
                MoodType.ANGRY   -> binding.chipAngry.isChecked = true
            }

            // Optional detail chips
            selectChipByText(
                binding.sleepChipGroup,
                m.sleep?.name?.lowercase()?.replaceFirstChar { it.uppercase() }
            )
            selectChipByText(
                binding.socialChipGroup,
                m.social?.name?.lowercase()?.replaceFirstChar { it.uppercase() }
            )
            selectChipByText(
                binding.hobbyChipGroup,
                m.hobby?.name?.lowercase()?.replaceFirstChar { it.uppercase() }
            )
            selectChipByText(
                binding.foodChipGroup,
                m.food?.name
                    ?.lowercase()
                    ?.replace('_', ' ')
                    ?.replaceFirstChar { it.uppercase() }
            )

            // Note + button label
            binding.note.setText(m.note)
            binding.btnAdd.text = getString(R.string.update)

            // Existing photo
            m.photoUri?.let {
                selectedPhotoUri = Uri.parse(it)
                showPhoto(selectedPhotoUri!!)
            } ?: run {
                binding.btnAddPhoto.text = getString(R.string.button_add_photo)
            }

            // Existing location
            currentLocation = m.location
            if (currentLocation != null) {
                binding.btnSetLocation.text = "Location ✓"
            }
        } ?: run {
            // Create mode – set default button text
            binding.btnAddPhoto.text = getString(R.string.button_add_photo)
        }

        // --- Buttons ---

        // Save / update mood
        binding.btnAdd.setOnClickListener { onSaveClicked() }

        // Cancel
        binding.btnCancel.setOnClickListener { finish() }

        // Add / change photo
        binding.btnAddPhoto.setOnClickListener {
            pickImage.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }

        // Remove photo
        binding.btnRemovePhoto.setOnClickListener {
            selectedPhotoUri = null
            binding.photoPreview.visibility = View.GONE
            binding.btnRemovePhoto.visibility = View.GONE
            binding.btnAddPhoto.text = getString(R.string.button_add_photo)
        }

        // Location picker button
        binding.btnSetLocation.setOnClickListener {
            val intent = Intent(this, MoodLocationPickerActivity::class.java).apply {
                currentLocation?.let { putExtra("location", it) }
            }
            mapIntentLauncher.launch(intent)
        }
    }

    // ----------------- SAVE / UPDATE -----------------

    private fun onSaveClicked() {
        val selectedType = selectedMoodTypeOrNull()
        if (selectedType == null) {
            Snackbar.make(binding.root, "Please select a mood!", Snackbar.LENGTH_SHORT).show()
            return
        }

        val sleep  = sleepFromChip(selectedChipText(binding.sleepChipGroup))
        val social = socialFromChip(selectedChipText(binding.socialChipGroup))
        val hobby  = hobbyFromChip(selectedChipText(binding.hobbyChipGroup))
        val food   = foodFromChip(selectedChipText(binding.foodChipGroup))

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
                timestamp = timestamp,
                photoUri = selectedPhotoUri?.toString(),
                location = currentLocation
            )
            app.moods.create(newMood)
            Snackbar.make(binding.root, "Mood added!", Snackbar.LENGTH_SHORT).show()
        } else {
            // UPDATE – keep same id + timestamp
            val updated = editingMood!!.copy(
                type = selectedType,
                note = binding.note.text?.toString().orEmpty(),
                sleep = sleep,
                social = social,
                hobby = hobby,
                food = food,
                timestamp = editingMood!!.timestamp,
                photoUri = selectedPhotoUri?.toString(),
                location = currentLocation
            )
            app.moods.update(updated)
            Snackbar.make(binding.root, "Mood updated!", Snackbar.LENGTH_SHORT).show()
        }

        setResult(RESULT_OK)
        finish()
    }

    // ----------------- HELPERS -----------------

    private fun wireSingleSelectChips(vararg chips: Chip) {
        chips.forEach { chip ->
            chip.setOnCheckedChangeListener { button, isChecked ->
                if (isChecked) {
                    chips.filter { it.id != button.id }.forEach { it.isChecked = false }
                }
            }
        }
    }

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

    private fun selectedChipText(group: ChipGroup): String? {
        val id = group.checkedChipId
        if (id == -1) return null
        val chip = group.findViewById<Chip>(id)
        return chip?.text?.toString()
    }

    private fun sleepFromChip(text: String?): SleepQuality? =
        text?.let { SleepQuality.valueOf(it.uppercase()) }

    private fun socialFromChip(text: String?): SocialActivity? =
        text?.let { SocialActivity.valueOf(it.uppercase()) }

    private fun hobbyFromChip(text: String?): Hobby? =
        text?.let { Hobby.valueOf(it.uppercase()) }

    private fun foodFromChip(text: String?): FoodType? =
        text?.let { FoodType.valueOf(it.replace(" ", "_").uppercase()) }

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

    private fun showPhoto(uri: Uri) {
        binding.photoPreview.load(uri)
        binding.photoPreview.visibility = View.VISIBLE
        binding.btnAddPhoto.text = getString(R.string.button_change_photo)
        binding.btnRemovePhoto.visibility = View.VISIBLE
    }

    private fun registerMapCallback() {
        mapIntentLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                val loc = result.data!!.getParcelableExtra<Location>("location")
                if (loc != null) {
                    currentLocation = loc
                    binding.btnSetLocation.text = "Location ✓"
                }
            }
        }
    }
}
