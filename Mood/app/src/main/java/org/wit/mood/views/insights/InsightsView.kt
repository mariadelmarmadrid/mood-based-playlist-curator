package org.wit.mood.views.insights

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.google.android.material.snackbar.Snackbar
import org.wit.mood.R
import org.wit.mood.databinding.ActivityInsightsBinding
import org.wit.mood.main.MainApp
import org.wit.mood.models.MoodType
import org.wit.mood.views.mood.MoodView
import org.wit.mood.views.moodlist.MoodListView

class InsightsView : AppCompatActivity(), InsightsContract.View {

    private lateinit var binding: ActivityInsightsBinding
    private lateinit var presenter: InsightsPresenter

    private val getResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) presenter.load()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInsightsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        presenter = InsightsPresenter(this, application as MainApp)

        // bottom nav
        binding.bottomNav.selectedItemId = R.id.nav_chart
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> { openHome(); true }
                R.id.nav_chart -> true
                else -> false
            }
        }
        binding.bottomNav.setOnItemReselectedListener { }

        // FAB add mood
        binding.fabAdd.setOnClickListener { openAddMood() }

        // day nav
        binding.btnPrevDay.setOnClickListener { presenter.onPrevDay() }
        binding.btnNextDay.setOnClickListener { presenter.onNextDay() }

        // playlist
        binding.btnOpenPlaylist.setOnClickListener { presenter.onOpenPlaylist() }

        presenter.load()
    }

    // ---- Contract methods ----

    override fun showEmptyState() {
        binding.tvDate.text = getString(R.string.app_name)
        binding.tvAverage.text = "No data yet"
        binding.moodRing.setData(emptyMap(), "")
        binding.legend.removeAllViews()
    }

    override fun showDay(date: String, averageLabel: String) {
        binding.tvDate.text = date
        binding.tvAverage.text = "Average: $averageLabel"
    }

    override fun showPlaylistButton(show: Boolean) {
        binding.btnOpenPlaylist.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun enableDayNav(prevEnabled: Boolean, nextEnabled: Boolean) {
        binding.btnPrevDay.isEnabled = prevEnabled
        binding.btnNextDay.isEnabled = nextEnabled
    }

    override fun updateRing(counts: Map<MoodType, Int>, averageLabel: String) {
        binding.moodRing.setData(counts, averageLabel)
    }

    override fun renderLegend(counts: Map<MoodType, Int>) {
        binding.legend.removeAllViews()

        val moodToIcon = mapOf(
            MoodType.HAPPY to R.drawable.ic_mood_happy_selector,
            MoodType.RELAXED to R.drawable.ic_mood_relaxed_selector,
            MoodType.NEUTRAL to R.drawable.ic_mood_neutral_selector,
            MoodType.SAD to R.drawable.ic_mood_sad_selector,
            MoodType.ANGRY to R.drawable.ic_mood_angry_selector
        )

        val moodToColor = mapOf(
            MoodType.HAPPY to ContextCompat.getColor(this, R.color.mood_happy),
            MoodType.RELAXED to ContextCompat.getColor(this, R.color.mood_relaxed),
            MoodType.NEUTRAL to ContextCompat.getColor(this, R.color.mood_neutral),
            MoodType.SAD to ContextCompat.getColor(this, R.color.mood_sad),
            MoodType.ANGRY to ContextCompat.getColor(this, R.color.mood_angry)
        )

        MoodType.values().forEach { mood ->
            val item = layoutInflater.inflate(R.layout.view_legend_item, binding.legend, false)
            val icon = item.findViewById<ImageView>(R.id.icon)
            val badge = item.findViewById<TextView>(R.id.badge)
            val label = item.findViewById<TextView>(R.id.label)

            icon.setImageResource(moodToIcon[mood]!!)
            icon.imageTintList = ColorStateList.valueOf(moodToColor[mood]!!)

            label.text = mood.label.lowercase()

            val n = counts[mood] ?: 0
            badge.text = n.toString()
            ViewCompat.setBackgroundTintList(badge, ColorStateList.valueOf(moodToColor[mood]!!))
            badge.visibility = if (n > 0) View.VISIBLE else View.INVISIBLE

            binding.legend.addView(item)
        }
    }

    override fun openUrlInSpotifyOrBrowser(url: String) {
        val uri = Uri.parse(url)

        val spotifyIntent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.spotify.music")
        }
        try {
            startActivity(spotifyIntent)
            return
        } catch (_: ActivityNotFoundException) { }

        val webIntent = Intent(Intent.ACTION_VIEW, uri)
        try {
            startActivity(webIntent)
        } catch (_: Exception) {
            Snackbar.make(binding.root, getString(R.string.error_no_spotify), Snackbar.LENGTH_SHORT)
                .show()
        }
    }

    override fun openHome() {
        startActivity(Intent(this, MoodListView::class.java))
        overridePendingTransition(0, 0)
        finish()
    }

    override fun openAddMood() {
        getResult.launch(Intent(this, MoodView::class.java))
    }
}
