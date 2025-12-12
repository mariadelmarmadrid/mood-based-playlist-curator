package org.wit.mood.views.moodlist

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import org.wit.mood.R
import org.wit.mood.adapters.DailyMoodAdapter
import org.wit.mood.databinding.ActivityMoodListBinding

class MoodListView : AppCompatActivity() {

    private lateinit var binding: ActivityMoodListBinding
    private lateinit var presenter: MoodListPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMoodListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        presenter = MoodListPresenter(this)

        setSupportActionBar(binding.topAppBar)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        // ✅ Bottom navigation wiring (THIS fixes chart icon doing nothing)
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

        binding.fabAdd.setOnClickListener { presenter.openAddMood() }

        renderList()
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

    // ✅ Make this public so presenter can refresh
    fun renderList() {
        val moods = presenter.loadMoods()
        val days = presenter.groupByDay(moods)

        binding.recyclerView.adapter = DailyMoodAdapter(
            days = days,
            app = application as org.wit.mood.main.MainApp,
            onEditClick = { presenter.openEditMood(it) },
            onDataChanged = { renderList() }
        )
    }
}
