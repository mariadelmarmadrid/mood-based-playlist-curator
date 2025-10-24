package org.wit.mood.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.doOnLayout
import androidx.recyclerview.widget.LinearLayoutManager
import org.wit.mood.R
import org.wit.mood.adapters.DailyMoodAdapter
import org.wit.mood.databinding.ActivityMoodListBinding
import org.wit.mood.main.MainApp
import org.wit.mood.models.DailyMoodSummary
import org.wit.mood.models.MoodModel
import timber.log.Timber.i
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Home screen: shows moods grouped by day with filtering:
 * - keyword search (in note)
 * - date range presets (All / Today / Last 7)
 * - minimum daily average score slider
 *
 * Uses DailyMoodAdapter to render "day cards", each containing its moods.
 */

class MoodListActivity : AppCompatActivity() {

    private lateinit var app: MainApp
    private lateinit var binding: ActivityMoodListBinding

    // ---- Filter state (single source of truth for the UI) ----
    private var allMoods: List<MoodModel> = emptyList()
    private var queryText: String = ""             // search within NOTE only
    private var dateFrom: LocalDate? = null        // null → no lower bound
    private var dateTo: LocalDate? = null          // null → no upper bound
    private var minDailyAvg: Double = -2.0         // -2.0 → treat as "All"

    private val dayFmt: DateTimeFormatter by lazy { DateTimeFormatter.ISO_LOCAL_DATE } // "yyyy-MM-dd"
    private var filterVisible = false

    // Re-launch add/edit screen and refresh on RESULT_OK
    private val getResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                allMoods = app.moods.findAll()
                refreshList()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMoodListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        app = application as MainApp

        // Toolbar (static, with filter icon action)
        setSupportActionBar(binding.topAppBar)

        // RecyclerView layout
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        // Initial data from store
        allMoods = app.moods.findAll()

        // --- Search (note text only) ---
        binding.inputSearchNotes.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                queryText = s?.toString().orEmpty()
                refreshList()
            }
        })

        // --- Date presets (mutually independent; each sets range then refreshes) ---
        binding.chipAllDates.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                dateFrom = null
                dateTo = null
                refreshList()
            }
        }
        binding.chipToday.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                val today = LocalDate.now()
                dateFrom = today
                dateTo = today
                refreshList()
            }
        }
        binding.chipLast7.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                val today = LocalDate.now()
                dateFrom = today.minusDays(6) // inclusive 7-day window [today-6, today]
                dateTo = today
                refreshList()
            }
        }

        // --- Minimum average slider (updates label + refilters) ---
        binding.sliderMinAvg.addOnChangeListener { _, value, _ ->
            val pos = value.toInt()                    // -2..2
            val threshold = thresholdFor(pos)
            val label = moodLabelFor(pos)

            minDailyAvg = threshold ?: -2.0

            binding.avgLabel.text =
                if (threshold == null) "Min daily average: All"
                else "Min daily average: $label"

            refreshList()
        }

        // --- Filter panel actions ---
        binding.btnApply.setOnClickListener {
            refreshList()
            hideFilterPanel()
            binding.recyclerView.scrollToPosition(0)
        }
        binding.btnReset.setOnClickListener {
            // Reset widgets → sync state → refresh
            binding.inputSearchNotes.setText("")
            binding.chipAllDates.isChecked = true
            binding.sliderMinAvg.value = -2f
            queryText = ""
            dateFrom = null
            dateTo = null
            minDailyAvg = -2.0
            refreshList()
            hideFilterPanel()
        }

        // First render and compute top padding after toolbar/layout pass
        refreshList()
        binding.topAppBar.doOnLayout { adjustRecyclerTopPadding() }

        // Bottom navigation (Home ↔ Insights) — stays in-place (no animation)
        binding.bottomNav.selectedItemId = R.id.nav_home
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_chart -> {
                    startActivity(Intent(this, InsightsActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                else -> false
            }
        }
        binding.bottomNav.setOnItemReselectedListener { /* no-op: keep position */ }

        // FAB → open add screen and refresh upon return
        binding.fabAdd.setOnClickListener {
            getResult.launch(Intent(this, MoodActivity::class.java))
        }
    }

    // ------- Toolbar menu (filter button) -------
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_top, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_filter -> { toggleFilterPanel(); true }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // ------- Filtering pipeline (pure-ish functions + adapter wiring) -------

    /**
     * Applies keyword → groups into day summaries → applies date/avg filters
     * then swaps the adapter. Called whenever a filter input changes.
     */
    private fun refreshList() {
        val keywordFiltered = applyKeywordFilter(allMoods, queryText)
        val summaries = toDailySummaries(keywordFiltered)
        val finalDays = filterSummaries(summaries, dateFrom, dateTo, minDailyAvg)

        binding.recyclerView.adapter = DailyMoodAdapter(
            days = finalDays,
            app = app,
            onEditClick = { mood ->
                val intent = Intent(this, MoodActivity::class.java).apply {
                    putExtra("mood_edit", mood)
                }
                getResult.launch(intent)
            },
            onDataChanged = {
                // Called when a mood is deleted/edited from inside an item
                allMoods = app.moods.findAll()
                refreshList()
            }
        )
        i("Recycler updated with ${finalDays.size} daily summaries (filters applied)")
    }

    /**
     * Filters notes by a case-insensitive substring match.
     * Only the NOTE field is searched (not enums or timestamp).
     */
    private fun applyKeywordFilter(source: List<MoodModel>, query: String): List<MoodModel> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return source
        return source.filter { m -> m.note.orEmpty().lowercase().contains(q) }
    }

    /**
     * Groups moods by date (yyyy-MM-dd), sorts each day's moods by time desc,
     * and computes the day's average mood score.
     */
    private fun toDailySummaries(list: List<MoodModel>): List<DailyMoodSummary> {
        val grouped = list.groupBy { it.timestamp.take(10) } // "yyyy-MM-dd"
        return grouped.map { (date, moods) ->
            val sorted = moods.sortedByDescending { it.timestamp }
            val avg = if (sorted.isNotEmpty()) sorted.map { it.type.score }.average() else 0.0
            DailyMoodSummary(date = date, moods = sorted, averageScore = avg)
        }.sortedByDescending { it.date }
    }

    /**
     * Applies the date-range filter (inclusive) and minimum daily average filter.
     * If parsing fails, the date is treated as valid to avoid hiding data unexpectedly.
     */
    private fun filterSummaries(
        days: List<DailyMoodSummary>,
        from: LocalDate?,
        to: LocalDate?,
        minAvg: Double
    ): List<DailyMoodSummary> {
        return days.filter { d ->
            val ld = runCatching { LocalDate.parse(d.date, dayFmt) }.getOrNull()
            val okDate = if (ld == null) true
            else {
                val gteFrom = from?.let { !ld.isBefore(it) } ?: true // ≥ from
                val lteTo   = to?.let   { !ld.isAfter(it) }  ?: true // ≤ to
                gteFrom && lteTo
            }
            val okAvg = (minAvg <= -2.0) || (d.averageScore >= minAvg)
            okDate && okAvg
        }
    }

    // ------- Filter panel show/hide with small slide animation -------

    /** Toggles the visibility of the filter card with a short animation. */
    private fun toggleFilterPanel() {
        if (filterVisible) hideFilterPanel() else showFilterPanel()
    }

    /** Slides the filter card down into view; recalculates top padding afterward. */
    private fun showFilterPanel() {
        if (binding.filterCard.visibility == View.VISIBLE) return
        binding.filterCard.visibility = View.VISIBLE
        binding.filterCard.alpha = 0f
        binding.filterCard.doOnLayout {           // wait until measured to get correct height
            binding.filterCard.translationY = -binding.filterCard.height.toFloat()
            binding.filterCard.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(180)
                .withEndAction { adjustRecyclerTopPadding() }
                .start()
        }
        filterVisible = true
    }

    /** Slides the filter card up and hides it; then adjusts top padding. */
    private fun hideFilterPanel() {
        if (binding.filterCard.visibility != View.VISIBLE) return
        // height is known here because it's visible
        binding.filterCard.animate()
            .alpha(0f)
            .translationY(-binding.filterCard.height.toFloat())
            .setDuration(160)
            .withEndAction {
                binding.filterCard.visibility = View.GONE
                adjustRecyclerTopPadding()
            }
            .start()
        filterVisible = false
    }


    /**
     * Adjusts RecyclerView padding so content never sits under the toolbar or FAB+BottomNav.
     * - Top padding = filter height (if visible) + a small extra dp.
     * - Bottom padding = BottomNav height + half FAB + its margin + extra dp.
     */
    private fun adjustRecyclerTopPadding() {
        val filterH = if (binding.filterCard.visibility == View.VISIBLE) binding.filterCard.height else 0
        val topPad = if (filterH > 0) filterH + dp(8) else 0 // ONLY extra for filter

        // Compute bottom padding after BottomNav/FAB have laid out
        binding.bottomNav.doOnLayout {
            binding.fabAdd.doOnLayout {
                val fabHalf = binding.fabAdd.height / 2
                val fabMb = (binding.fabAdd.layoutParams as ViewGroup.MarginLayoutParams).bottomMargin
                val bottomPad = binding.bottomNav.height + fabHalf + fabMb + dp(16)
                binding.recyclerView.setPadding(
                    binding.recyclerView.paddingLeft,
                    topPad,
                    binding.recyclerView.paddingRight,
                    bottomPad
                )
            }
        }
    }

    /** Density helper: dp(int) → px(int). */
    private fun dp(px: Int): Int = (px * resources.displayMetrics.density).toInt()

    /**
     * Maps the slider’s integer position (-2..2) to a human-readable mood label.
     *
     * Used only for display on the UI (e.g., "Min daily average: Happy 😊").
     * Each position represents a qualitative range of average mood scores:
     *   2 → Happy, 1 → Relaxed, 0 → Neutral, -1 → Sad, -2 → All (no filter)
     */
    private fun moodLabelFor(pos: Int): String = when (pos) {
        2 -> "Happy 😊"
        1 -> "Relaxed 😌"
        0 -> "Neutral 😐"
        -1 -> "Sad 😢"
        -2 -> "All"
        else -> "All"
    }

    /**
     * Converts the slider’s integer position (-2..2) to the corresponding
     * minimum average score threshold used by the filter.
     *
     * Returns:
     *   - A Double threshold (e.g. 1.5 for "Happy") when filtering by mood level.
     *   - null when "All" is selected, meaning no filter should be applied.
     *
     * This ensures that qualitative labels ("Happy", "Relaxed", etc.)
     * are correctly mapped to their numeric cutoff values used internally.
     */
    private fun thresholdFor(pos: Int): Double? = when (pos) {
        2 -> 1.5
        1 -> 0.5
        0 -> -0.5
        -1 -> -1.5
        -2 -> null     // null means "All"
        else -> null
    }


}
