package org.wit.mood.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import org.wit.mood.R
import org.wit.mood.adapters.MoodAdapter // only if you use flat list somewhere
import org.wit.mood.databinding.ActivityMoodListBinding
import org.wit.mood.main.MainApp
import org.wit.mood.models.DailyMoodSummary
import org.wit.mood.models.MoodModel
import org.wit.mood.adapters.DailyMoodAdapter   // ← your grouped-by-day adapter
import org.wit.mood.adapters.MoodListener
import timber.log.Timber.i

class MoodListActivity : AppCompatActivity(), MoodListener {

    lateinit var app: MainApp
    private lateinit var binding: ActivityMoodListBinding

    private val getResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                updateRecyclerView()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMoodListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.title = title
        setSupportActionBar(binding.toolbar)

        app = application as MainApp

        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        updateRecyclerView()
    }

    // ---- Toolbar menu (add new mood) ----
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.item_add -> {
                val intent = Intent(this, MoodActivity::class.java)
                getResult.launch(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // ---- Click from adapter: open editor with selected mood ----
    override fun onMoodClick(mood: MoodModel) {
        val intent = Intent(this, MoodActivity::class.java)
        intent.putExtra("mood_edit", mood)  // Parcelable MoodModel
        getResult.launch(intent)
    }

    // ---- Build & render daily summaries ----
    private fun updateRecyclerView() {
        val summaries = getDailySummaries()
        // DailyMoodAdapter should accept a MoodListener and call onMoodClick(mood) for item taps
        val adapter = DailyMoodAdapter(summaries, app, listener = this)
        binding.recyclerView.adapter = adapter
        i("Recycler updated with ${summaries.size} daily summaries")
    }

    private fun getDailySummaries(): List<DailyMoodSummary> {
        val all = app.moods.findAll()
        val grouped = all.groupBy { it.timestamp.take(10) } // "yyyy-MM-dd"
        return grouped.map { (date, moods) ->
            val avgScore = if (moods.isNotEmpty()) moods.map { it.type.score }.average() else 0.0
            DailyMoodSummary(date = date, moods = moods, averageScore = avgScore)
        }.sortedByDescending { it.date } // newest day first
    }
}
