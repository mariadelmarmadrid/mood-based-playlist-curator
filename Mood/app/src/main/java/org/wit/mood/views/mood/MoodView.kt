package org.wit.mood.views.mood

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.google.android.material.snackbar.Snackbar
import org.wit.mood.R
import org.wit.mood.databinding.ActivityMoodBinding
import org.wit.mood.models.*
import timber.log.Timber.i

class MoodView : AppCompatActivity() {

    private lateinit var binding: ActivityMoodBinding
    private lateinit var presenter: MoodPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMoodBinding.inflate(layoutInflater)
        setContentView(binding.root)

        wireSingleSelectChips(
            binding.chipHappy,
            binding.chipRelaxed,
            binding.chipNeutral,
            binding.chipSad,
            binding.chipAngry
        )

        presenter = MoodPresenter(this)

        if (!presenter.edit) {
            binding.chipNeutral.isChecked = true
        }

        // Save/Add
        binding.btnAdd.setOnClickListener {
            val type = selectedMoodTypeOrNull()
            if (type == null) {
                Snackbar.make(binding.root, "Please select a mood!", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            presenter.doAddOrSave(
                type = type,
                note = binding.note.text.toString(),
                sleep = sleepFromChip(selectedChipText(binding.sleepChipGroup)),
                social = socialFromChip(selectedChipText(binding.socialChipGroup)),
                hobby = hobbyFromChip(selectedChipText(binding.hobbyChipGroup)),
                food = foodFromChip(selectedChipText(binding.foodChipGroup))
            )
        }

        // Cancel
        binding.btnCancel.setOnClickListener { presenter.doCancel() }

        // Photo
        binding.btnAddPhoto.setOnClickListener {
            presenter.cacheMood(
                selectedMoodTypeOrNull(),
                binding.note.text.toString(),
                sleepFromChip(selectedChipText(binding.sleepChipGroup)),
                socialFromChip(selectedChipText(binding.socialChipGroup)),
                hobbyFromChip(selectedChipText(binding.hobbyChipGroup)),
                foodFromChip(selectedChipText(binding.foodChipGroup))
            )
            presenter.doSelectImage()
        }

        binding.btnRemovePhoto.setOnClickListener { presenter.doRemovePhoto() }

        // Location
        binding.btnSetLocation.setOnClickListener {
            presenter.cacheMood(
                selectedMoodTypeOrNull(),
                binding.note.text.toString(),
                sleepFromChip(selectedChipText(binding.sleepChipGroup)),
                socialFromChip(selectedChipText(binding.socialChipGroup)),
                hobbyFromChip(selectedChipText(binding.hobbyChipGroup)),
                foodFromChip(selectedChipText(binding.foodChipGroup))
            )
            presenter.doSetLocation()
        }
    }

    // ---------- Called by presenter ----------

    fun showMood(mood: MoodModel) {
        binding.note.setText(mood.note)
        binding.btnAdd.text = getString(R.string.update)

        // main mood chip
        when (mood.type) {
            MoodType.HAPPY -> binding.chipHappy.isChecked = true
            MoodType.RELAXED -> binding.chipRelaxed.isChecked = true
            MoodType.NEUTRAL -> binding.chipNeutral.isChecked = true
            MoodType.SAD -> binding.chipSad.isChecked = true
            MoodType.ANGRY -> binding.chipAngry.isChecked = true
        }

        // photo
        mood.photoUri?.let { updatePhoto(it) }

        // location tick
        showLocationTick(mood.location != null)
    }

    fun updatePhoto(uriString: String) {
        i("Photo updated")
        binding.photoPreview.load(Uri.parse(uriString))
        binding.photoPreview.visibility = android.view.View.VISIBLE
        binding.btnRemovePhoto.visibility = android.view.View.VISIBLE
        binding.btnAddPhoto.text = getString(R.string.button_change_photo)
    }

    fun hidePhoto() {
        binding.photoPreview.visibility = android.view.View.GONE
        binding.btnRemovePhoto.visibility = android.view.View.GONE
        binding.btnAddPhoto.text = getString(R.string.button_add_photo)
    }

    fun showLocationTick(selected: Boolean) {
        binding.btnSetLocation.text =
            if (selected) "Location ✓" else getString(R.string.button_set_location)
    }

    // ---------- helpers to read form ----------

    private fun selectedMoodTypeOrNull(): MoodType? {
        val checked = listOf(
            binding.chipHappy, binding.chipRelaxed, binding.chipNeutral, binding.chipSad, binding.chipAngry
        ).firstOrNull { it.isChecked } ?: return null

        val labelFromTag = (checked.tag as? String).orEmpty()
        return MoodType.values().firstOrNull { it.label == labelFromTag }
    }

    private fun selectedChipText(group: com.google.android.material.chip.ChipGroup): String? {
        val id = group.checkedChipId
        if (id == -1) return null
        val chip = group.findViewById<com.google.android.material.chip.Chip>(id)
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

    private fun wireSingleSelectChips(vararg chips: com.google.android.material.chip.Chip) {
        chips.forEach { chip ->
            chip.setOnCheckedChangeListener { button, isChecked ->
                if (isChecked) {
                    chips.filter { it.id != button.id }
                        .forEach { it.isChecked = false }
                }
            }
        }
    }

}
