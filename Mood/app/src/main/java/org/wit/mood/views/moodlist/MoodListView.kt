package org.wit.mood.views.moodlist

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.widget.doAfterTextChanged
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
 * Activity implementing the View role in the MVP (Model–View–Presenter)
 * architecture for the Mood List screen.
 *
 * Responsibilities:
 *  - Display mood entries grouped by day
 *  - Handle all UI interactions (search, filters, navigation)
 *  - Launch Android activities and handle activity results
 *  - Delegate all business logic to the Presenter
 */
class MoodListView : AppCompatActivity(), MoodListContract.View {

    /** ViewBinding reference for accessing UI components. */
    private lateinit var binding: ActivityMoodListBinding

    /** Presenter responsible for business logic and state management. */
    private lateinit var presenter: MoodListPresenter

    /** Tracks whether the filter card is currently visible. */
    private var filterVisible = false

    /** Activity result launcher for adding/editing moods. */
    private lateinit var moodEditorLauncher: ActivityResultLauncher<Intent>

    // ---------------------------------------------------------------------
    // Lifecycle
    // ---------------------------------------------------------------------

    /**
     * Called when the Activity is created.
     *
     * Initialises:
     *  - ViewBinding
     *  - Presenter
     *  - UI components and listeners
     *  - Activity result handling
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMoodListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Create presenter and attach this View
        presenter = MoodListPresenter(this, this)

        // Use Material Top App Bar as the ActionBar
        setSupportActionBar(binding.topAppBar)

        // UI setup
        setupRecyclerView()
        setupBottomNavigation()
        setupFab()
        setupFilters()

        // Apply persisted night mode setting
        presenter.loadNightMode()

        // Register Activity Result launcher for Mood editor
        moodEditorLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    presenter.loadMoods()
                }
            }
    }

    // ---------------------------------------------------------------------
    // Setup methods
    // ---------------------------------------------------------------------

    /**
     * Configures the RecyclerView layout.
     */
    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
    }

    /**
     * Configures the bottom navigation bar.
     */
    private fun setupBottomNavigation() {
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
    }

    /**
     * Configures the Floating Action Button for adding a new mood.
     */
    private fun setupFab() {
        binding.fabAdd.setOnClickListener {
            presenter.openAddMood()
        }
    }

    /**
     * Configures all filter-related UI interactions.
     *
     * Filtering behaviour provides instant feedback,
     * matching Assignment 1 behaviour.
     */
    private fun setupFilters() {

        // Instant text search filtering
        binding.inputSearchNotes.doAfterTextChanged { text ->
            presenter.onSearchQueryChanged(text?.toString().orEmpty())
            presenter.applyFilters()
        }

        // Date filter chips (All / Today / Last 7 Days)
        binding.chipsDate.setOnCheckedStateChangeListener { _, checkedIds ->
            when (checkedIds.firstOrNull()) {
                R.id.chipToday ->
                    presenter.onDateFilterChanged(DateFilter.TODAY)
                R.id.chipLast7 ->
                    presenter.onDateFilterChanged(DateFilter.LAST_7_DAYS)
                else ->
                    presenter.onDateFilterChanged(DateFilter.ALL)
            }
            presenter.applyFilters()
        }

        // Minimum daily average slider
        binding.sliderMinAvg.addOnChangeListener { _, value, _ ->
            presenter.onMinAverageChanged(value.toInt())
            binding.avgLabel.text = "Min daily average: ${value.toInt()}"
            presenter.applyFilters()
        }

        // Apply button applies filters and closes filter card
        binding.btnApply.setOnClickListener {
            presenter.applyFilters()
            binding.filterCard.visibility = View.GONE
            filterVisible = false
        }

        // Reset button clears both filter logic and UI state
        binding.btnReset.setOnClickListener {

            // Reset presenter filter state
            presenter.resetFilters()

            // Reset UI controls to default values
            binding.inputSearchNotes.setText("")
            binding.chipsDate.check(R.id.chipAllDates)
            binding.sliderMinAvg.value = -2f
            binding.avgLabel.text = "Min daily average: -2"

            // Close filter card
            binding.filterCard.visibility = View.GONE
            filterVisible = false
        }
    }

    // ---------------------------------------------------------------------
    // Top menu
    // ---------------------------------------------------------------------

    /**
     * Inflates the top app bar menu.
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_top, menu)
        return true
    }

    /**
     * Handles actions from the top app bar menu.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_filter -> {
                filterVisible = !filterVisible
                binding.filterCard.visibility =
                    if (filterVisible) View.VISIBLE else View.GONE
                true
            }
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

    // ---------------------------------------------------------------------
    // Contract methods
    // ---------------------------------------------------------------------

    /**
     * Renders a list of daily mood summaries using a RecyclerView adapter.
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
     * Launches the Mood editor screen for adding or editing a mood.
     */
    override fun launchMoodEditor(mood: MoodModel?) {
        val intent = Intent(this, MoodView::class.java)
        mood?.let { intent.putExtra("mood_edit", it) }
        moodEditorLauncher.launch(intent)
    }

    /**
     * Navigates to the Map screen.
     */
    override fun navigateToMap() {
        startActivity(Intent(this, MoodMapActivity::class.java))
    }

    /**
     * Navigates to the Insights screen.
     */
    override fun navigateToInsights() {
        startActivity(Intent(this, InsightsView::class.java))
    }

    /**
     * Applies night mode to the Activity.
     *
     * Recreates the Activity only if the requested
     * mode differs from the current configuration.
     */
    override fun applyNightMode(enabled: Boolean) {
        val desired = if (enabled)
            AppCompatDelegate.MODE_NIGHT_YES
        else
            AppCompatDelegate.MODE_NIGHT_NO

        val currentNight =
            resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK

        val desiredNight =
            if (enabled) Configuration.UI_MODE_NIGHT_YES
            else Configuration.UI_MODE_NIGHT_NO

        if (currentNight == desiredNight) return

        AppCompatDelegate.setDefaultNightMode(desired)
        recreate()
    }

    /**
     * Closes the current Activity.
     */
    override fun finishView() {
        finish()
    }
}
