package org.wit.mood.views.mood

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
import org.wit.mood.R
import org.wit.mood.databinding.ActivityMoodBinding
import org.wit.mood.models.*

class MoodView : AppCompatActivity(), MoodContract.View {

    private lateinit var binding: ActivityMoodBinding
    private lateinit var presenter: MoodPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMoodBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Make emoji chips behave like single-select and never allow "none selected"
        wireSingleSelectChips(
            binding.chipHappy,
            binding.chipRelaxed,
            binding.chipNeutral,
            binding.chipSad,
            binding.chipAngry
        )

        presenter = MoodPresenter(this)

        // Default mood ONLY for create mode
        if (!presenter.edit) {
            binding.chipNeutral.isChecked = true
        }

        // Save/Add
        binding.btnAdd.setOnClickListener {
            val type = selectedMoodTypeOrNull()
            if (type == null) {
                showError("Please select a mood!")
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

        // Photo (cache form state first)
        binding.btnAddPhoto.setOnClickListener {
            presenter.cacheMood(
                type = selectedMoodTypeOrNull(),
                note = binding.note.text.toString(),
                sleep = sleepFromChip(selectedChipText(binding.sleepChipGroup)),
                social = socialFromChip(selectedChipText(binding.socialChipGroup)),
                hobby = hobbyFromChip(selectedChipText(binding.hobbyChipGroup)),
                food = foodFromChip(selectedChipText(binding.foodChipGroup))
            )
            presenter.doSelectImage()
        }

        binding.btnRemovePhoto.setOnClickListener { presenter.doRemovePhoto() }

        // Location (cache form state first)
        binding.btnSetLocation.setOnClickListener {
            presenter.cacheMood(
                type = selectedMoodTypeOrNull(),
                note = binding.note.text.toString(),
                sleep = sleepFromChip(selectedChipText(binding.sleepChipGroup)),
                social = socialFromChip(selectedChipText(binding.socialChipGroup)),
                hobby = hobbyFromChip(selectedChipText(binding.hobbyChipGroup)),
                food = foodFromChip(selectedChipText(binding.foodChipGroup))
            )
            presenter.doSetLocation()
        }
    }

    // ---------- Contract methods ----------

    override fun showMood(mood: MoodModel) {
        binding.note.setText(mood.note)
        binding.btnAdd.text = getString(R.string.update)

        when (mood.type) {
            MoodType.HAPPY   -> binding.chipHappy.isChecked = true
            MoodType.RELAXED -> binding.chipRelaxed.isChecked = true
            MoodType.NEUTRAL -> binding.chipNeutral.isChecked = true
            MoodType.SAD     -> binding.chipSad.isChecked = true
            MoodType.ANGRY   -> binding.chipAngry.isChecked = true
        }

        mood.photoUri?.let { updatePhoto(it) } ?: hidePhoto()
        showLocationTick(mood.location != null)
    }

    override fun updatePhoto(uriString: String) {
        binding.photoPreview.load(Uri.parse(uriString))
        binding.photoPreview.visibility = View.VISIBLE
        binding.btnRemovePhoto.visibility = View.VISIBLE
        binding.btnAddPhoto.text = getString(R.string.button_change_photo)
    }

    override fun hidePhoto() {
        binding.photoPreview.visibility = View.GONE
        binding.btnRemovePhoto.visibility = View.GONE
        binding.btnAddPhoto.text = getString(R.string.button_add_photo)
    }

    override fun showLocationTick(show: Boolean) {
        binding.btnSetLocation.text =
            if (show) "Location ✓" else getString(R.string.button_set_location)
    }

    override fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    override fun finishView() {
        finish()
    }

    // ---------- helpers ----------

    private fun selectedMoodTypeOrNull(): MoodType? {
        val checked = listOf(
            binding.chipHappy,
            binding.chipRelaxed,
            binding.chipNeutral,
            binding.chipSad,
            binding.chipAngry
        ).firstOrNull { it.isChecked } ?: return null

        val labelFromTag = (checked.tag as? String).orEmpty()
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

    /**
     * Single-select behaviour + prevents "none selected":
     * if user tries to uncheck the last selected chip, re-check it.
     */
    private fun wireSingleSelectChips(vararg chips: Chip) {
        chips.forEach { chip ->
            chip.setOnCheckedChangeListener { button, isChecked ->
                if (isChecked) {
                    chips.filter { it.id != button.id }.forEach { it.isChecked = false }
                } else {
                    if (chips.none { it.isChecked }) button.isChecked = true
                }
            }
        }
    }
}
