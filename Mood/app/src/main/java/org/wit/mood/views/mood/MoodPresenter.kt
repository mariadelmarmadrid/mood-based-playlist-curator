package org.wit.mood.views.mood

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import org.wit.mood.main.MainApp
import org.wit.mood.models.*
import org.wit.mood.views.location.LocationPickerView
import timber.log.Timber
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * MoodPresenter
 *
 * Presenter implementation for the Mood feature following the
 * Model–View–Presenter (MVP) architecture.
 *
 * This class:
 *  - Handles all business logic for creating and editing moods
 *  - Communicates with the data layer via MainApp
 *  - Coordinates system interactions such as image picking and location selection
 *  - Updates the View through the MoodContract.View interface
 */
class MoodPresenter(private val view: MoodView) : MoodContract.Presenter {

    // Currently active mood being created or edited
    var mood = MoodModel()

    // Reference to application-level data store
    private val app: MainApp = view.application as MainApp

    // Activity result launchers for image picker and map picker
    private lateinit var imageIntentLauncher: ActivityResultLauncher<PickVisualMediaRequest>
    private lateinit var mapIntentLauncher: ActivityResultLauncher<Intent>

    // Indicates whether the presenter is editing an existing mood
    var edit: Boolean = false
        private set

    /**
     * Initialisation block.
     *
     * Determines whether the presenter is in edit mode,
     * restores the existing mood if applicable,
     * and registers required activity result callbacks.
     */
    init {
        if (view.intent.hasExtra("mood_edit")) {
            edit = true
            mood = view.intent.extras?.getParcelable("mood_edit")!!
            view.showMood(mood)
        }

        registerImagePickerCallback()
        registerMapCallback()
    }

    /**
     * Creates a new mood or updates an existing one.
     *
     * Collects all user input, updates the MoodModel,
     * persists it using the data store,
     * and closes the view.
     */
    override fun doAddOrSave(
        type: MoodType,
        note: String,
        sleep: SleepQuality?,
        social: SocialActivity?,
        hobby: Hobby?,
        food: FoodType?
    ) {
        // Update mood fields from user input
        mood.type = type
        mood.note = note
        mood.sleep = sleep
        mood.social = social
        mood.hobby = hobby
        mood.food = food

        // Assign a timestamp only when creating a new mood
        if (!edit) {
            mood.timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        }

        // Persist mood to the data store
        if (edit) app.moods.update(mood) else app.moods.create(mood)

        // Notify caller and close the screen
        view.setResult(AppCompatActivity.RESULT_OK)
        view.finishView()
    }

    /**
     * Cancels the operation and closes the view.
     */
    override fun doCancel() = view.finishView()

    /**
     * Launches the system image picker for selecting a mood photo.
     */
    override fun doSelectImage() {
        val request = PickVisualMediaRequest.Builder()
            .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly)
            .build()
        imageIntentLauncher.launch(request)
    }

    /**
     * Launches the location picker view.
     *
     * Uses the existing mood location if available,
     * otherwise defaults to a predefined location.
     */
    override fun doSetLocation() {
        val start = mood.location ?: Location(52.245696, -7.139102, 15f)
        val launcherIntent = Intent(view, LocationPickerView::class.java)
            .putExtra("location", start)
        mapIntentLauncher.launch(launcherIntent)
    }

    /**
     * Removes the currently selected photo from the mood.
     */
    override fun doRemovePhoto() {
        mood.photoUri = null
        view.hidePhoto()
    }

    /**
     * Temporarily stores user input while navigating away
     * from the screen (e.g., when picking an image or location).
     */
    override fun cacheMood(
        type: MoodType?,
        note: String,
        sleep: SleepQuality?,
        social: SocialActivity?,
        hobby: Hobby?,
        food: FoodType?
    ) {
        if (type != null) mood.type = type
        mood.note = note
        mood.sleep = sleep
        mood.social = social
        mood.hobby = hobby
        mood.food = food
    }

    /**
     * Shares the current mood using a system share intent.
     *
     * Formats the mood details into readable text.
     */
    override fun doShare() {
        val date = mood.timestamp.takeIf { it.length >= 10 }?.take(10) ?: "N/A"
        val note = mood.note.ifBlank { "No note" }
        val loc = mood.location?.let { "Location: ${it.lat}, ${it.lng}" } ?: "Location: none"

        val text =
            "${mood.type.label}\n" +
                    "Date: $date\n" +
                    "Note: $note\n" +
                    loc

        view.launchShareIntent(text)
    }

    /**
     * Registers the callback for the image picker activity result.
     *
     * Stores the selected image URI and updates the view.
     */
    private fun registerImagePickerCallback() {
        imageIntentLauncher = view.registerForActivityResult(
            ActivityResultContracts.PickVisualMedia()
        ) { uri ->
            try {
                if (uri != null) {
                    view.contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                    mood.photoUri = uri.toString()
                    Timber.i("IMG :: ${mood.photoUri}")
                    view.updatePhoto(mood.photoUri!!)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                view.showError("Could not load photo.")
            }
        }
    }

    /**
     * Registers the callback for the location picker activity result.
     *
     * Retrieves the selected location and updates the mood model.
     */
    private fun registerMapCallback() {
        mapIntentLauncher =
            view.registerForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) { result ->
                if (result.resultCode == AppCompatActivity.RESULT_OK) {
                    val loc = result.data?.extras?.getParcelable<Location>("location")
                    if (loc != null) {
                        Timber.i("Location == $loc")
                        mood.location = loc
                        view.showLocationTick(true)
                    }
                }
            }
    }
}
