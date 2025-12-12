package org.wit.mood.activities

import android.net.Uri
import org.wit.mood.main.MainApp
import org.wit.mood.models.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MoodPresenter(
    private val view: MoodView,
    private val app: MainApp
) {

    fun init(editingMood: MoodModel?) {
        // Edit mode: prefill UI
        if (editingMood != null) {
            view.setUpdateMode(true)
            view.setMoodSelected(editingMood.type)
            view.setNote(editingMood.note)

            // Photo
            editingMood.photoUri?.let {
                val uri = Uri.parse(it)
                view.setSelectedPhotoUri(uri)
                view.showPhoto(uri)
            } ?: view.hidePhoto()

            // Location
            view.setCurrentLocation(editingMood.location)
            view.setLocationButtonChecked(editingMood.location != null)
        } else {
            // Create mode defaults
            view.setUpdateMode(false)
            view.hidePhoto()
            view.setLocationButtonChecked(false)
        }
    }

    fun onAddPhotoClicked(launchPicker: () -> Unit) {
        launchPicker()
    }

    fun onPhotoPicked(uri: Uri) {
        view.setSelectedPhotoUri(uri)
        view.showPhoto(uri)
    }

    fun onRemovePhotoClicked() {
        view.setSelectedPhotoUri(null)
        view.hidePhoto()
    }

    fun onLocationPicked(location: Location) {
        view.setCurrentLocation(location)
        view.setLocationButtonChecked(true)
    }

    fun onSaveClicked() {
        val selectedType = view.getSelectedMoodTypeOrNull()
        if (selectedType == null) {
            view.showMessage("Please select a mood!")
            return
        }

        val sleep  = sleepFromChip(view.getSelectedSleepText())
        val social = socialFromChip(view.getSelectedSocialText())
        val hobby  = hobbyFromChip(view.getSelectedHobbyText())
        val food   = foodFromChip(view.getSelectedFoodText())

        val note = view.getNoteText()
        val photoUri = view.getSelectedPhotoUriOrNull()?.toString()
        val location = view.getCurrentLocationOrNull()

        val editingMood = view.getEditingMoodOrNull()

        if (editingMood == null) {
            val timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

            val newMood = MoodModel(
                type = selectedType,
                note = note,
                sleep = sleep,
                social = social,
                hobby = hobby,
                food = food,
                timestamp = timestamp,
                photoUri = photoUri,
                location = location
            )
            app.moods.create(newMood)
            view.showMessage("Mood added!")
        } else {
            val updated = editingMood.copy(
                type = selectedType,
                note = note,
                sleep = sleep,
                social = social,
                hobby = hobby,
                food = food,
                timestamp = editingMood.timestamp,
                photoUri = photoUri,
                location = location
            )
            app.moods.update(updated)
            view.showMessage("Mood updated!")
        }

        view.closeWithOkResult()
    }

    fun onCancelClicked() {
        // Just close (no RESULT_OK)
        // Your Activity can simply finish(), but keeping it here is clean MVP.
        // If you want: view.closeWithoutResult()
    }

    // ------- mapping helpers -------
    private fun sleepFromChip(text: String?): SleepQuality? =
        text?.let { SleepQuality.valueOf(it.uppercase()) }

    private fun socialFromChip(text: String?): SocialActivity? =
        text?.let { SocialActivity.valueOf(it.uppercase()) }

    private fun hobbyFromChip(text: String?): Hobby? =
        text?.let { Hobby.valueOf(it.uppercase()) }

    private fun foodFromChip(text: String?): FoodType? =
        text?.let { FoodType.valueOf(it.replace(" ", "_").uppercase()) }
}
