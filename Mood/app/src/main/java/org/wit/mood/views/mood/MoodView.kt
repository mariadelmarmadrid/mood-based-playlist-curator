package org.wit.mood.views.mood

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
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

        // ✅ REQUIRED FOR MENU
        setSupportActionBar(binding.topAppBar)

        wireSingleSelectChips(
            binding.chipHappy,
            binding.chipRelaxed,
            binding.chipNeutral,
            binding.chipSad,
            binding.chipAngry
        )

        presenter = MoodPresenter(this)

        if (!presenter.edit) binding.chipNeutral.isChecked = true

        binding.btnAdd.setOnClickListener {
            val type = selectedMoodTypeOrNull()
            if (type == null) {
                showError("Please select a mood")
                return@setOnClickListener
            }

            presenter.doAddOrSave(
                type,
                binding.note.text.toString(),
                sleepFromChip(selectedChipText(binding.sleepChipGroup)),
                socialFromChip(selectedChipText(binding.socialChipGroup)),
                hobbyFromChip(selectedChipText(binding.hobbyChipGroup)),
                foodFromChip(selectedChipText(binding.foodChipGroup))
            )
        }

        binding.btnCancel.setOnClickListener { presenter.doCancel() }

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

    // ---------- MENU ----------
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_mood, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.item_share -> {

                // ✅ Update presenter.mood with CURRENT UI selections before sharing
                presenter.cacheMood(
                    type = selectedMoodTypeOrNull(),
                    note = binding.note.text.toString(),
                    sleep = sleepFromChip(selectedChipText(binding.sleepChipGroup)),
                    social = socialFromChip(selectedChipText(binding.socialChipGroup)),
                    hobby = hobbyFromChip(selectedChipText(binding.hobbyChipGroup)),
                    food = foodFromChip(selectedChipText(binding.foodChipGroup))
                )

                presenter.doShare()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // ---------- CONTRACT ----------
    override fun showMood(mood: MoodModel) {
        binding.note.setText(mood.note)
        when (mood.type) {
            MoodType.HAPPY -> binding.chipHappy.isChecked = true
            MoodType.RELAXED -> binding.chipRelaxed.isChecked = true
            MoodType.NEUTRAL -> binding.chipNeutral.isChecked = true
            MoodType.SAD -> binding.chipSad.isChecked = true
            MoodType.ANGRY -> binding.chipAngry.isChecked = true
        }

        mood.photoUri?.let { updatePhoto(it) } ?: hidePhoto()
        showLocationTick(mood.location != null)
    }

    override fun updatePhoto(uri: String) {
        binding.photoPreview.load(Uri.parse(uri))
        binding.photoPreview.visibility = View.VISIBLE
        binding.btnRemovePhoto.visibility = View.VISIBLE
    }

    override fun hidePhoto() {
        binding.photoPreview.visibility = View.GONE
        binding.btnRemovePhoto.visibility = View.GONE
    }

    override fun showLocationTick(show: Boolean) {
        binding.btnSetLocation.text =
            if (show) "Location ✓" else getString(R.string.button_set_location)
    }

    override fun launchShareIntent(text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        startActivity(Intent.createChooser(intent, getString(R.string.share)))
    }

    override fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    override fun finishView() {
        finish()
    }

    // ---------- HELPERS ----------
    private fun selectedMoodTypeOrNull(): MoodType? {
        val checkedChip = listOf(
            binding.chipHappy,
            binding.chipRelaxed,
            binding.chipNeutral,
            binding.chipSad,
            binding.chipAngry
        ).firstOrNull { it.isChecked } ?: return null

        val tag = checkedChip.tag as? String ?: return null
        return MoodType.values().firstOrNull { it.label == tag }
    }

    private fun wireSingleSelectChips(vararg chips: Chip) {
        chips.forEach { chip ->
            chip.setOnCheckedChangeListener { button, isChecked ->
                if (isChecked) {
                    chips.filter { it.id != button.id }.forEach { it.isChecked = false }
                } else {
                    // prevent none selected
                    if (chips.none { it.isChecked }) button.isChecked = true
                }
            }
        }
    }

    private fun selectedChipText(group: ChipGroup): String? {
        val id = group.checkedChipId
        if (id == View.NO_ID || id == -1) return null
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
}
