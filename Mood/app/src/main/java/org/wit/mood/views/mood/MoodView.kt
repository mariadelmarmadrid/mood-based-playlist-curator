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

/**
 * MoodView
 *
 * Activity responsible for displaying and collecting user input
 * when creating or editing a mood entry.
 *
 * Acts as the View in the MVP architecture:
 *  - Delegates business logic to MoodPresenter
 *  - Renders UI state and feedback
 *  - Handles user interaction events
 */
class MoodView : AppCompatActivity(), MoodContract.View {

    // ViewBinding for accessing UI elements safely
    private lateinit var binding: ActivityMoodBinding

    // Presenter handling all business logic
    private lateinit var presenter: MoodPresenter

    /**
     * Called when the activity is created.
     *
     * Sets up the UI, wires event listeners,
     * and initializes the presenter.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate layout using ViewBinding
        binding = ActivityMoodBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set toolbar as the app bar (required for menu actions)
        setSupportActionBar(binding.topAppBar)

        // Ensure mood chips behave as a single-select group
        wireSingleSelectChips(
            binding.chipHappy,
            binding.chipRelaxed,
            binding.chipNeutral,
            binding.chipSad,
            binding.chipAngry
        )

        // Initialize presenter
        presenter = MoodPresenter(this)

        // Default mood selection when creating a new entry
        if (!presenter.edit) binding.chipNeutral.isChecked = true

        // Save/Add button
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

        // Cancel button
        binding.btnCancel.setOnClickListener { presenter.doCancel() }

        // Add photo button
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

        // Remove photo button
        binding.btnRemovePhoto.setOnClickListener { presenter.doRemovePhoto() }

        // Set location button
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

    /**
     * Inflates the options menu for the Mood screen.
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_mood, menu)
        return true
    }

    /**
     * Handles menu item selection events.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.item_share -> {

                // Update presenter state with current UI selections before sharing
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

    // ---------- CONTRACT IMPLEMENTATION ----------

    /**
     * Displays an existing mood when editing.
     */
    override fun showMood(mood: MoodModel) {
        binding.note.setText(mood.note)

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

    /**
     * Displays the selected photo in the UI.
     */
    override fun updatePhoto(uri: String) {
        binding.photoPreview.load(Uri.parse(uri))
        binding.photoPreview.visibility = View.VISIBLE
        binding.btnRemovePhoto.visibility = View.VISIBLE
    }

    /**
     * Hides the photo preview and remove button.
     */
    override fun hidePhoto() {
        binding.photoPreview.visibility = View.GONE
        binding.btnRemovePhoto.visibility = View.GONE
    }

    /**
     * Updates the location button to indicate whether a location is set.
     */
    override fun showLocationTick(show: Boolean) {
        binding.btnSetLocation.text =
            if (show) "Location ✓" else getString(R.string.button_set_location)
    }

    /**
     * Launches the system share intent.
     */
    override fun launchShareIntent(text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        startActivity(Intent.createChooser(intent, getString(R.string.share)))
    }

    /**
     * Displays an error message using a Snackbar.
     */
    override fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    /**
     * Closes the activity.
     */
    override fun finishView() {
        finish()
    }

    // ---------- HELPER FUNCTIONS ----------

    /**
     * Returns the currently selected MoodType, or null if none selected.
     */
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

    /**
     * Enforces single selection behaviour across multiple chips.
     */
    private fun wireSingleSelectChips(vararg chips: Chip) {
        chips.forEach { chip ->
            chip.setOnCheckedChangeListener { button, isChecked ->
                if (isChecked) {
                    chips.filter { it.id != button.id }
                        .forEach { it.isChecked = false }
                } else if (chips.none { it.isChecked }) {
                    // Prevent having no chip selected
                    button.isChecked = true
                }
            }
        }
    }

    /**
     * Retrieves the selected chip text from a ChipGroup.
     */
    private fun selectedChipText(group: ChipGroup): String? {
        val id = group.checkedChipId
        if (id == View.NO_ID || id == -1) return null
        val chip = group.findViewById<Chip>(id)
        return chip?.text?.toString()
    }

    /**
     * Converts chip text to corresponding enum values.
     */
    private fun sleepFromChip(text: String?): SleepQuality? =
        text?.let { SleepQuality.valueOf(it.uppercase()) }

    private fun socialFromChip(text: String?): SocialActivity? =
        text?.let { SocialActivity.valueOf(it.uppercase()) }

    private fun hobbyFromChip(text: String?): Hobby? =
        text?.let { Hobby.valueOf(it.uppercase()) }

    private fun foodFromChip(text: String?): FoodType? =
        text?.let { FoodType.valueOf(it.replace(" ", "_").uppercase()) }
}