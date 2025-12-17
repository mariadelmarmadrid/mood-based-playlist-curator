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
 * Activity implementing the Mood List screen (View in MVP pattern).
 */
class MoodListView : AppCompatActivity(), MoodListContract.View {

    private lateinit var binding: ActivityMoodListBinding
    private lateinit var presenter: MoodListPresenter
    private var filterVisible = false
    private lateinit var moodEditorLauncher: ActivityResultLauncher<Intent>

    // ---------------------------------------------------------------------
    // Lifecycle
    // ---------------------------------------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMoodListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        presenter = MoodListPresenter(this, this)

        setSupportActionBar(binding.topAppBar)

        setupRecyclerView()
        setupBottomNavigation()
        setupFab()
        setupFilters()

        presenter.loadNightMode()

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

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
    }

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

    private fun setupFab() {
        binding.fabAdd.setOnClickListener {
            presenter.openAddMood()
        }
    }

    private fun setupFilters() {

        // Search notes
        binding.inputSearchNotes.doAfterTextChanged { text ->
            presenter.onSearchQueryChanged(text?.toString().orEmpty())
            presenter.applyFilters()
        }

        // Date filter chips
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

        // Min daily average slider
        binding.sliderMinAvg.addOnChangeListener { _, value, _ ->
            presenter.onMinAverageChanged(value.toInt())
            binding.avgLabel.text = "Min daily average: ${value.toInt()}"
            presenter.applyFilters()
        }

        // Apply filters
        binding.btnApply.setOnClickListener {
            presenter.applyFilters()
            binding.filterCard.visibility = View.GONE
            filterVisible = false
        }


        // Reset filters
        binding.btnReset.setOnClickListener {

            // 1. Reset presenter state
            presenter.resetFilters()

            // 2. Reset search input
            binding.inputSearchNotes.setText("")

            // 3. Reset date chips to ALL
            binding.chipsDate.check(R.id.chipAllDates)

            // 4. Reset slider to default
            binding.sliderMinAvg.value = -2f
            binding.avgLabel.text = "Min daily average: -2"

            // 5. Close filter card
            binding.filterCard.visibility = View.GONE
            filterVisible = false
        }

    }

    // ---------------------------------------------------------------------
    // Top menu
    // ---------------------------------------------------------------------

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_top, menu)
        return true
    }

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

    override fun renderList(days: List<DailyMoodSummary>) {
        binding.recyclerView.adapter = DailyMoodAdapter(
            days = days,
            app = application as org.wit.mood.main.MainApp,
            onEditClick = { presenter.openEditMood(it) },
            onDataChanged = { presenter.loadMoods() }
        )
    }

    override fun launchMoodEditor(mood: MoodModel?) {
        val intent = Intent(this, MoodView::class.java)
        mood?.let { intent.putExtra("mood_edit", it) }
        moodEditorLauncher.launch(intent)
    }


    override fun navigateToMap() {
        startActivity(Intent(this, MoodMapActivity::class.java))
    }

    override fun navigateToInsights() {
        startActivity(Intent(this, InsightsView::class.java))
    }

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

    override fun finishView() {
        finish()
    }
}
