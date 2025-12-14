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

class MoodListPresenter(private val view: MoodListView) : MoodListContract.Presenter {

    private val app: MainApp = view.application as MainApp

    private val prefs: SharedPreferences =
        view.getSharedPreferences("settings", Context.MODE_PRIVATE)

    private lateinit var moodEditorLauncher: ActivityResultLauncher<Intent>

    init {
        registerMoodEditorCallback()
        loadMoods()
    }

    override fun loadMoods() {
        val moods = app.moods.findAll()

        val days = moods
            .groupBy { it.timestamp.take(10) }
            .map { (date, moodsInDay) ->
                val sorted = moodsInDay.sortedByDescending { it.timestamp }
                val avg = if (sorted.isNotEmpty()) sorted.map { it.type.score }.average() else 0.0
                DailyMoodSummary(date, sorted, avg)
            }
            .sortedByDescending { it.date }

        view.renderList(days)
    }

    override fun openAddMood() {
        view.launchMoodEditor()
    }

    override fun openEditMood(mood: MoodModel) {
        view.launchMoodEditor(mood)
    }

    override fun openMap() {
        view.navigateToMap()
    }

    override fun openInsights() {
        view.navigateToInsights()
        view.finishView()
    }

    override fun loadNightMode() {
        val enabled = prefs.getBoolean("night_mode", false)
        view.applyNightMode(enabled)
    }

    override fun doToggleNightMode() {
        val current = prefs.getBoolean("night_mode", false)
        val next = !current
        prefs.edit().putBoolean("night_mode", next).apply()
        view.applyNightMode(next)
    }

    private fun registerMoodEditorCallback() {
        moodEditorLauncher =
            view.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == AppCompatActivity.RESULT_OK) {
                    loadMoods()
                }
            }
    }

    override fun launchEditor(intent: Intent) {
        moodEditorLauncher.launch(intent)
    }
}
