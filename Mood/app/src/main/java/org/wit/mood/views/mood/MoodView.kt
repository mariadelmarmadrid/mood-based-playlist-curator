package org.wit.mood.activities

import android.net.Uri
import org.wit.mood.models.Location
import org.wit.mood.models.MoodModel
import org.wit.mood.models.MoodType

interface MoodView {
    fun showMessage(message: String)
    fun closeWithOkResult()

    // UI setters / state
    fun setUpdateMode(isUpdate: Boolean)
    fun setMoodSelected(type: MoodType)
    fun setNote(text: String)
    fun setLocationButtonChecked(checked: Boolean)

    fun showPhoto(uri: Uri)
    fun hidePhoto()

    // UI getters (Presenter reads input via View)
    fun getNoteText(): String
    fun getSelectedMoodTypeOrNull(): MoodType?
    fun getSelectedSleepText(): String?
    fun getSelectedSocialText(): String?
    fun getSelectedHobbyText(): String?
    fun getSelectedFoodText(): String?

    // When we are editing, presenter may need to know current mood
    fun getEditingMoodOrNull(): MoodModel?

    // Location + photo state
    fun getSelectedPhotoUriOrNull(): Uri?
    fun getCurrentLocationOrNull(): Location?
    fun setCurrentLocation(location: Location?)
    fun setSelectedPhotoUri(uri: Uri?)
}
