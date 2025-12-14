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

class MoodPresenter(private val view: MoodView) : MoodContract.Presenter {

    var mood = MoodModel()
    private val app: MainApp = view.application as MainApp

    private lateinit var imageIntentLauncher: ActivityResultLauncher<PickVisualMediaRequest>
    private lateinit var mapIntentLauncher: ActivityResultLauncher<Intent>

    var edit: Boolean = false
        private set

    init {
        if (view.intent.hasExtra("mood_edit")) {
            edit = true
            mood = view.intent.extras?.getParcelable("mood_edit")!!
            view.showMood(mood)
        }

        registerImagePickerCallback()
        registerMapCallback()
    }

    override fun doAddOrSave(
        type: MoodType,
        note: String,
        sleep: SleepQuality?,
        social: SocialActivity?,
        hobby: Hobby?,
        food: FoodType?
    ) {
        mood.type = type
        mood.note = note
        mood.sleep = sleep
        mood.social = social
        mood.hobby = hobby
        mood.food = food

        if (!edit) {
            mood.timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        }

        if (edit) app.moods.update(mood) else app.moods.create(mood)

        view.setResult(AppCompatActivity.RESULT_OK)
        view.finishView()
    }

    override fun doCancel() = view.finishView()

    override fun doSelectImage() {
        val request = PickVisualMediaRequest.Builder()
            .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly)
            .build()
        imageIntentLauncher.launch(request)
    }

    override fun doSetLocation() {
        val start = mood.location ?: Location(52.245696, -7.139102, 15f)
        val launcherIntent = Intent(view, LocationPickerView::class.java)
            .putExtra("location", start)
        mapIntentLauncher.launch(launcherIntent)
    }

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

    override fun doRemovePhoto() {
        mood.photoUri = null
        view.hidePhoto()
    }

    // ---------------- callbacks ----------------

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

    private fun registerMapCallback() {
        mapIntentLauncher =
            view.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                when (result.resultCode) {
                    AppCompatActivity.RESULT_OK -> {
                        val loc = result.data?.extras?.getParcelable<Location>("location")
                        if (loc != null) {
                            Timber.i("Location == $loc")
                            mood.location = loc
                            view.showLocationTick(true)
                        }
                    }
                    else -> { /* ignore */ }
                }
            }
    }
}
