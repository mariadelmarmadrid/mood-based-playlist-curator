package org.wit.mood.views.moodlist

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import org.wit.mood.R
import org.wit.mood.activities.MoodMapActivity
import org.wit.mood.adapters.DailyMoodAdapter
import org.wit.mood.databinding.ActivityMoodListBinding
import org.wit.mood.models.DailyMoodSummary
import org.wit.mood.models.MoodModel
import org.wit.mood.views.insights.InsightsView
import org.wit.mood.views.mood.MoodView

/**
 * MoodListView
 *
 * Activity implementing the Mood List screen (View in MVP pattern).
 *
 * Responsibilities:
 *  - Displays the list of moods grouped by day
 *  - Handles UI interactions (FAB, bottom navigation, top menu)
 *  - Delegates actions to the presenter
 *  - Supports night mode theming
 */
class MoodListView : AppCompatActivity(), MoodListContract.View {

    private lateinit var binding: ActivityMoodListBinding
    private lateinit var presenter: MoodListPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMoodListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize the presenter
        presenter = MoodListPresenter(this)

        // Set toolbar as ActionBar
        setSupportActionBar(binding.topAppBar)

        // Configure RecyclerView with LinearLayoutManager
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        // ---------- Bottom navigation ----------
        binding.bottomNav.selectedItemId = R.id.nav_home
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_chart -> {
                    presenter.openInsights()
                    true
                }
                else -> false
            }
        }
        binding.bottomNav.setOnItemReselectedListener { }

        // Floating action button opens Add Mood screen
        binding.fabAdd.setOnClickListener { presenter.openAddMood() }

        // Apply saved night mode setting
        presenter.loadNightMode()
    }

    // ---------- Top menu ----------
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_top, menu) // Inflate menu once
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_map -> {
                presenter.openMap()
                true
            }
            R.id.action_toggle_theme -> {
                presenter.doToggleNightMode()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // ---------- Contract methods (View interface) ----------

    /**
     * Renders the list of daily moods in the RecyclerView.
     *
     * @param days List of DailyMoodSummary objects to display
     */
    override fun renderList(days: List<DailyMoodSummary>) {
        binding.recyclerView.adapter = DailyMoodAdapter(
            days = days,
            app = application as org.wit.mood.main.MainApp,
            onEditClick = { presenter.openEditMood(it) },
            onDataChanged = { presenter.loadMoods() }
        )
    }

    /**
     * Launches the Mood editor Activity.
     *
     * @param mood Optional MoodModel to edit; null opens Add Mood
     */
    override fun launchMoodEditor(mood: MoodModel?) {
        val intent = Intent(this, MoodView::class.java)
        mood?.let { intent.putExtra("mood_edit", it) }
        presenter.launchEditor(intent)
    }

    /** Navigates to the Map view. */
    override fun navigateToMap() {
        startActivity(Intent(this, MoodMapActivity::class.java))
    }

    /** Navigates to the Insights screen. */
    override fun navigateToInsights() {
        startActivity(Intent(this, InsightsView::class.java))
    }

    /**
     * Applies night mode to the Activity.
     *
     * Uses AppCompatDelegate and avoids unnecessary recreation if
     * the desired mode matches the current mode.
     *
     * @param enabled Whether night mode should be enabled
     */
    override fun applyNightMode(enabled: Boolean) {
        val desired = if (enabled) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO

        // Determine current night mode
        val currentNight = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val desiredNight = if (enabled) Configuration.UI_MODE_NIGHT_YES else Configuration.UI_MODE_NIGHT_NO

        // Only apply if different from current
        if (currentNight == desiredNight) return

        AppCompatDelegate.setDefaultNightMode(desired)
        recreate() // Refresh activity to apply new theme
    }

    /** Finishes the current Activity. */
    override fun finishView() {
        finish()
    }
}
