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
    private lateinit var map: GoogleMap
    private lateinit var app: MainApp

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

        val moodsWithLocation = app.moods.findAll().filter { it.location != null }

        var firstPosition: LatLng? = null

        for (mood in moodsWithLocation) {
            val loc = mood.location!!
            val pos = LatLng(loc.lat, loc.lng)
            if (firstPosition == null) firstPosition = pos

            val title = "${mood.type.label} mood"   // e.g. "Happy 😊 mood"
            val snippet = buildString {
                if (mood.note.isNotBlank()) {
                    append("Note: ${mood.note}\n")
                }
                append("Date: ${mood.timestamp}")
            }

            map.addMarker(
                MarkerOptions()
                    .position(pos)
                    .title(title)
                    .snippet(snippet)
            )
        }

        // Center camera
        val center = firstPosition ?: LatLng(52.2460, -7.1390)  // fallback = SETU
        val zoom = moodsWithLocation.firstOrNull()?.location?.zoom ?: 15f
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(center, zoom))
    }
}
