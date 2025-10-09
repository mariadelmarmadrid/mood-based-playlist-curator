package org.wit.mood.activities

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.wit.mood.R
import org.wit.mood.databinding.ActivityMoodListBinding
import org.wit.mood.databinding.CardMoodBinding
import org.wit.mood.main.MainApp
import org.wit.mood.models.DailyMoodSummary
import org.wit.mood.models.MoodModel
import timber.log.Timber.i

class MoodListActivity : AppCompatActivity() {

    lateinit var app: MainApp
    private lateinit var binding: ActivityMoodListBinding
    private lateinit var moodAdapter: MoodAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMoodListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.title = title
        setSupportActionBar(binding.toolbar)

        app = application as MainApp

        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        // Load daily summary cards
        updateRecyclerView()
    }


    private fun getDailySummaries(): List<DailyMoodSummary> {
        val grouped = app.moods.findAll().groupBy { it.timestamp.substring(0, 10) }

        return grouped.map { (date, moods) ->
            val avgScore = if (moods.isNotEmpty()) moods.map { it.type.score }.average() else 0.0
            DailyMoodSummary(date, moods, avgScore)
        }.sortedByDescending { it.date } // optional: show newest first
    }



    fun updateRecyclerView() {
        val summaries = getDailySummaries()
        val dailyAdapter = DailyMoodAdapter(summaries, app) // pass app
        binding.recyclerView.adapter = dailyAdapter
    }



    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.item_add -> {
                val launcherIntent = Intent(this, MoodActivity::class.java)
                getResult.launch(launcherIntent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private val getResult =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == RESULT_OK) {
                updateRecyclerView()
            }
        }
}

class MoodAdapter(
    private val moods: MutableList<MoodModel>,
    private val onDeleteClick: (MoodModel) -> Unit
) : RecyclerView.Adapter<MoodAdapter.MainHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainHolder {
        val binding = CardMoodBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MainHolder(binding)
    }

    override fun onBindViewHolder(holder: MainHolder, position: Int) {
        val mood = moods[holder.adapterPosition]
        holder.bind(mood, onDeleteClick)
    }

    override fun getItemCount(): Int = moods.size

    fun updateMoods(newMoods: List<MoodModel>) {
        moods.clear()
        moods.addAll(newMoods)
        notifyDataSetChanged()
    }

    class MainHolder(private val binding: CardMoodBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(mood: MoodModel, onDeleteClick: (MoodModel) -> Unit) {
            binding.moodTitle.text = mood.type.label
            binding.moodTimestamp.text = mood.timestamp
            binding.note.text = mood.note
            binding.sleep.text = "🛌 ${mood.sleep.name.lowercase()}"
            binding.social.text = "👥 ${mood.social.name.lowercase()}"
            binding.hobby.text = "🎨 ${mood.hobby.name.lowercase()}"
            binding.food.text = "🍽️ ${mood.food.name.lowercase()}"

            binding.btnDelete.setOnClickListener {
                val context = binding.root.context
                android.app.AlertDialog.Builder(context)
                    .setTitle("Delete Mood")
                    .setMessage("Are you sure you want to delete this mood?")
                    .setPositiveButton("Yes") { _, _ -> onDeleteClick(mood) }
                    .setNegativeButton("No", null)
                    .show()
            }
        }
    }
}
