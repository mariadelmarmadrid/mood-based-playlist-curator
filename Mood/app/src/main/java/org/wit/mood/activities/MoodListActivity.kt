package org.wit.mood.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import org.wit.mood.R
import org.wit.mood.adapters.DailyMoodAdapter
import org.wit.mood.adapters.MoodListener
import org.wit.mood.databinding.ActivityMoodListBinding
import org.wit.mood.main.MainApp
import org.wit.mood.models.DailyMoodSummary
import org.wit.mood.models.MoodModel
import timber.log.Timber.i

class MoodListActivity : AppCompatActivity(), MoodListener {

    private lateinit var app: MainApp
    private lateinit var binding: ActivityMoodListBinding

    private val getResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) updateRecyclerView()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMoodListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        app = application as MainApp

        // --- RecyclerView ---
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        updateRecyclerView()

        // --- Bottom Nav ---
        binding.bottomNav.selectedItemId = R.id.nav_home   // <-- IMPORTANT: we're on Home

        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true // already here
                R.id.nav_chart -> {
                    startActivity(Intent(this, InsightsActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                else -> false
            }
        }
        binding.bottomNav.setOnItemReselectedListener { /* no-op */ }


        // --- FAB → Add new mood ---
        binding.fabAdd.setOnClickListener {
            val intent = Intent(this, MoodActivity::class.java)
            getResult.launch(intent)
        }
    }

    // From adapter: edit an existing mood
    override fun onMoodClick(mood: MoodModel) {
        val intent = Intent(this, MoodActivity::class.java).apply {
            putExtra("mood_edit", mood)
        }
        getResult.launch(intent)
    }

    // Build & render daily summaries
    private fun updateRecyclerView() {
        val summaries = getDailySummaries()
        binding.recyclerView.adapter = DailyMoodAdapter(
            days = summaries,
            app = app,
            listener = this,
            onDataChanged = { updateRecyclerView() } // refresh after delete/update
        )
        i("Recycler updated with ${summaries.size} daily summaries")
    }

    private fun getDailySummaries(): List<DailyMoodSummary> {
        val all = app.moods.findAll()
        val grouped = all.groupBy { it.timestamp.take(10) } // yyyy-MM-dd per-day

        return grouped.map { (date, moods) ->
            val moodsSorted = moods.sortedByDescending { it.timestamp } // newest first in the day
            val avgScore = if (moodsSorted.isNotEmpty())
                moodsSorted.map { it.type.score }.average() else 0.0
            DailyMoodSummary(date = date, moods = moodsSorted, averageScore = avgScore)
        }.sortedByDescending { it.date } // newest day first
    }
}
