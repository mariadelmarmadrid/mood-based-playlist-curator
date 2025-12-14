package org.wit.mood.views.moodlist

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import org.wit.mood.R
import org.wit.mood.adapters.DailyMoodAdapter
import org.wit.mood.databinding.ActivityMoodListBinding
import org.wit.mood.models.DailyMoodSummary
import org.wit.mood.models.MoodModel
import org.wit.mood.activities.MoodMapActivity
import org.wit.mood.views.insights.InsightsView
import org.wit.mood.views.mood.MoodView

class MoodListView : AppCompatActivity(), MoodListContract.View {

    private lateinit var binding: ActivityMoodListBinding
    private lateinit var presenter: MoodListPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMoodListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        presenter = MoodListPresenter(this)

        setSupportActionBar(binding.topAppBar)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        // Bottom navigation
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

        binding.fabAdd.setOnClickListener {
            presenter.openAddMood()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_top, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_map -> {
                presenter.openMap()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // ---------- Contract methods ----------

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
        presenter.launchEditor(intent)
    }

    override fun navigateToMap() {
        startActivity(Intent(this, MoodMapActivity::class.java))
    }

    override fun navigateToInsights() {
        startActivity(Intent(this, InsightsView::class.java))
    }

    override fun finishView() {
        finish()
    }
}
