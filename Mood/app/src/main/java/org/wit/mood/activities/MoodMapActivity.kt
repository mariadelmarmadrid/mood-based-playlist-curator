package org.wit.mood.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import org.wit.mood.R
import org.wit.mood.databinding.ActivityMoodMapBinding
import org.wit.mood.main.MainApp
import org.wit.mood.models.MoodModel

class MoodMapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMoodMapBinding
    private lateinit var app: MainApp
    private lateinit var map: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMoodMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        app = application as MainApp

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.uiSettings.isZoomControlsEnabled = true

        val moodsWithLocation: List<MoodModel> =
            app.moods.findAll().filter { it.location != null }

        if (moodsWithLocation.isEmpty()) {
            val setu = LatLng(52.2460, -7.1390)
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(setu, 15f))
            return
        }

        moodsWithLocation.forEach { mood ->
            val loc = mood.location ?: return@forEach
            val pos = LatLng(loc.lat, loc.lng)

            // "yyyy-MM-dd HH:mm:ss" -> "yyyy-MM-dd"
            val shortDate = mood.timestamp.take(10)

            // Title now includes mood AND date
            val title = "${mood.type.label} • $shortDate"

            // Snippet just for the note (or fallback text)
            val snippet = if (mood.note.isNotBlank()) {
                "Note: ${mood.note}"
            } else {
                "No note"
            }

            map.addMarker(
                MarkerOptions()
                    .position(pos)
                    .title(title)
                    .snippet(snippet)
            )
        }

        val latest = moodsWithLocation.maxByOrNull { it.timestamp }
        latest?.location?.let { loc ->
            map.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(loc.lat, loc.lng),
                    loc.zoom
                )
            )
        }
    }
}
