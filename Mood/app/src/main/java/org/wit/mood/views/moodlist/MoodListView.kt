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
import org.wit.mood.models.MoodModel
import org.wit.mood.views.mood.MoodView

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

        renderList()

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
            R.id.action_toggle_theme -> {
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun renderList() {
        val moods = presenter.loadMoods()
        val days = presenter.groupByDay(moods)

        binding.recyclerView.adapter = DailyMoodAdapter(
            days = days,
            app = application as org.wit.mood.main.MainApp,
            onEditClick = { presenter.openEditMood(it) },
            onDataChanged = {
                // When adapter deletes/updates something, refresh the list
                renderList()
            }
        )
    }


    fun launchMoodEditor(mood: MoodModel?) {
        val intent = Intent(this, MoodView::class.java)
        mood?.let { intent.putExtra("mood_edit", it) }
        startActivity(intent)
    }
}
