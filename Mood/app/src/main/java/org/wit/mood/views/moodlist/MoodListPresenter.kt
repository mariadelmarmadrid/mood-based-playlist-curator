package org.wit.mood.views.moodlist

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import org.wit.mood.main.MainApp
import org.wit.mood.models.DailyMoodSummary
import org.wit.mood.models.MoodModel

/**
 * MoodListPresenter
 *
 * Presenter class for the Mood List feature (MVP pattern).
 *
 * Responsibilities:
 *  - Load moods from the data store
 *  - Group moods by day and calculate daily averages
 *  - Handle navigation to editor, map, and insights
 *  - Manage night mode settings
 */
class MoodListPresenter(private val view: MoodListView) : MoodListContract.Presenter {

    // Reference to the main app for accessing the Mood data store
    private val app: MainApp = view.application as MainApp

    // SharedPreferences for storing settings (e.g., night mode)
    private val prefs: SharedPreferences =
        view.getSharedPreferences("settings", Context.MODE_PRIVATE)

    // Launcher for starting the Mood editor activity and handling results
    private lateinit var moodEditorLauncher: ActivityResultLauncher<Intent>

    /**
     * Initialize presenter:
     *  - Registers callback for mood editor
     *  - Loads the initial list of moods
     */
    init {
        registerMoodEditorCallback()
        loadMoods()
    }

    /**
     * Loads all moods from the store, groups them by day,
     * sorts them, calculates average daily mood, and sends
     * the data to the view for rendering.
     */
    override fun loadMoods() {
        val moods = app.moods.findAll()

        val days = moods
            .groupBy { it.timestamp.take(10) } // Group by date (yyyy-MM-dd)
            .map { (date, moodsInDay) ->
                val sorted = moodsInDay.sortedByDescending { it.timestamp } // Latest first
                val avg = if (sorted.isNotEmpty()) sorted.map { it.type.score }.average() else 0.0
                DailyMoodSummary(date, sorted, avg) // Wrap into daily summary
            }
            .sortedByDescending { it.date } // Most recent days first

        view.renderList(days)
    }

    /** Opens the Mood editor to add a new mood. */
    override fun openAddMood() {
        view.launchMoodEditor()
    }

    /**
     * Opens the Mood editor to edit an existing mood.
     *
     * @param mood The MoodModel to edit
     */
    override fun openEditMood(mood: MoodModel) {
        view.launchMoodEditor(mood)
    }

    /** Navigates to the Map view. */
    override fun openMap() {
        view.navigateToMap()
    }

    /** Navigates to the Insights view and closes the list view. */
    override fun openInsights() {
        view.navigateToInsights()
        view.finishView()
    }

    /**
     * Loads the current night mode setting from SharedPreferences
     * and applies it to the view.
     */
    override fun loadNightMode() {
        val enabled = prefs.getBoolean("night_mode", false)
        view.applyNightMode(enabled)
    }

    /**
     * Toggles night mode on/off and updates both preferences
     * and the view.
     */
    override fun doToggleNightMode() {
        val current = prefs.getBoolean("night_mode", false)
        val next = !current
        prefs.edit().putBoolean("night_mode", next).apply()
        view.applyNightMode(next)
    }

    /**
     * Registers the callback for the Mood editor activity.
     *
     * When the editor returns RESULT_OK, the mood list is reloaded.
     */
    private fun registerMoodEditorCallback() {
        moodEditorLauncher =
            view.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == AppCompatActivity.RESULT_OK) {
                    loadMoods()
                }
            }
    }

    /**
     * Launches the Mood editor activity using the registered launcher.
     *
     * @param intent Intent to start the editor
     */
    override fun launchEditor(intent: Intent) {
        moodEditorLauncher.launch(intent)
    }
}
