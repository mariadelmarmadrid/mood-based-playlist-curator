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
import org.wit.mood.adapters.MoodListener
import org.wit.mood.databinding.ActivityMoodListBinding
import org.wit.mood.main.MainApp
import org.wit.mood.models.DailyMoodSummary
import org.wit.mood.models.MoodModel
import timber.log.Timber.i
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MoodListActivity : AppCompatActivity(), MoodListener {

    private lateinit var app: MainApp
    private lateinit var binding: ActivityMoodListBinding

    // ---- Filter state ----
    private var allMoods: List<MoodModel> = emptyList()
    private var queryText: String = ""             // keyword in NOTE only
    private var dateFrom: LocalDate? = null        // null = all dates
    private var dateTo: LocalDate? = null
    private var minDailyAvg: Double = -2.0         // -2.0 = no filter

    private val dayFmt: DateTimeFormatter by lazy { DateTimeFormatter.ISO_LOCAL_DATE } // "yyyy-MM-dd"
    private var filterVisible = false

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

        // Toolbar
        setSupportActionBar(binding.topAppBar)

        // Recycler
        binding.recyclerView.layoutManager = LinearLayoutManager(this)


        // Load data
        allMoods = app.moods.findAll()

        // --- Search (notes) ---
        binding.inputSearchNotes.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                queryText = s?.toString().orEmpty()
                refreshList()
            }
        })

        // --- Date chips ---
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
                dateFrom = today.minusDays(6) // inclusive 7-day window
                dateTo = today
                refreshList()
            }
        }

        // --- Min average slider ---
        binding.sliderMinAvg.addOnChangeListener { _, value, _ ->
            minDailyAvg = value.toDouble()
            binding.avgLabel.text =
                if (minDailyAvg <= -2.0) "Min daily average: All"
                else "Min daily average: ${"%.1f".format(minDailyAvg)}"
            refreshList()
        }

        // --- Filter actions ---
        binding.btnApply.setOnClickListener {
            refreshList()
            hideFilterPanel()
            binding.recyclerView.scrollToPosition(0)
        }
        binding.btnReset.setOnClickListener {
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

        // Initial render + padding for toolbar
        refreshList()
        binding.topAppBar.doOnLayout { adjustRecyclerTopPadding() }

        // Bottom Nav
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
        binding.bottomNav.setOnItemReselectedListener { /* no-op */ }

        // FAB
        binding.fabAdd.setOnClickListener {
            getResult.launch(Intent(this, MoodActivity::class.java))
        }
    }

    // ------- Toolbar menu -------
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

    // ------- Filtering pipeline -------
    private fun refreshList() {
        val keywordFiltered = applyKeywordFilter(allMoods, queryText)
        val summaries = toDailySummaries(keywordFiltered)
        val finalDays = filterSummaries(summaries, dateFrom, dateTo, minDailyAvg)

        binding.recyclerView.adapter = DailyMoodAdapter(
            days = finalDays,
            app = app,
            listener = this,
            onDataChanged = {
                allMoods = app.moods.findAll()
                refreshList()
            }
        )
        i("Recycler updated with ${finalDays.size} daily summaries (filters applied)")
    }

    private fun applyKeywordFilter(source: List<MoodModel>, query: String): List<MoodModel> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return source
        return source.filter { m -> m.note.orEmpty().lowercase().contains(q) }
    }

    private fun toDailySummaries(list: List<MoodModel>): List<DailyMoodSummary> {
        val grouped = list.groupBy { it.timestamp.take(10) } // "yyyy-MM-dd"
        return grouped.map { (date, moods) ->
            val sorted = moods.sortedByDescending { it.timestamp }
            val avg = if (sorted.isNotEmpty()) sorted.map { it.type.score }.average() else 0.0
            DailyMoodSummary(date = date, moods = sorted, averageScore = avg)
        }.sortedByDescending { it.date }
    }

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
                val gteFrom = from?.let { !ld.isBefore(it) } ?: true
                val lteTo   = to?.let   { !ld.isAfter(it) }  ?: true
                gteFrom && lteTo
            }
            val okAvg = (minAvg <= -2.0) || (d.averageScore >= minAvg)
            okDate && okAvg
        }
    }

    // ------- Panel show/hide + padding -------
    private fun toggleFilterPanel() {
        if (filterVisible) hideFilterPanel() else showFilterPanel()
    }

    private fun showFilterPanel() {
        if (binding.filterCard.visibility == View.VISIBLE) return
        binding.filterCard.visibility = View.VISIBLE
        binding.filterCard.alpha = 0f
        binding.filterCard.doOnLayout {           // <-- wait for measured height
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


    /** Sets list top padding to toolbar height (+ filter height if visible). */

    private fun adjustRecyclerTopPadding() {
        val filterH = if (binding.filterCard.visibility == View.VISIBLE) binding.filterCard.height else 0
        val topPad = if (filterH > 0) filterH + dp(8) else 0 // ONLY extra for filter
        // Keep your bottom padding logic (BottomNav + FAB) as-is:
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


    private fun dp(px: Int): Int = (px * resources.displayMetrics.density).toInt()

    // Adapter callback
    override fun onMoodClick(mood: MoodModel) {
        val intent = Intent(this, MoodActivity::class.java).apply {
            putExtra("mood_edit", mood)
        }
        getResult.launch(intent)
    }
}
