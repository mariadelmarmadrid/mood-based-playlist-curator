package org.wit.mood.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import org.wit.mood.R
import org.wit.mood.databinding.ActivityMoodMapBinding
import org.wit.mood.models.Location

class MoodMapActivity : AppCompatActivity(),
    OnMapReadyCallback,
    GoogleMap.OnMarkerDragListener {

    private lateinit var map: GoogleMap
    private lateinit var binding: ActivityMoodMapBinding

    // Location we are editing / choosing
    private var location: Location = Location(
        lat = 52.2457,
        lng = -7.1391,
        zoom = 15f
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMoodMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // If caller passed an existing location, use it
        intent.getParcelableExtra<Location>("location")?.let {
            location = it
        }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // When user presses back, return the chosen location
        onBackPressedDispatcher.addCallback(this) {
            val returnIntent = Intent().apply {
                putExtra("location", location)
            }
            setResult(Activity.RESULT_OK, returnIntent)
            finish()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        map.uiSettings.isZoomControlsEnabled = true

        val startLatLng = LatLng(location.lat, location.lng)

        // Draggable marker the user can move
        val markerOptions = MarkerOptions()
            .position(startLatLng)
            .title("Mood location")
            .snippet("Drag to adjust")
            .draggable(true)

        map.addMarker(markerOptions)
        map.setOnMarkerDragListener(this)

        map.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                startLatLng,
                location.zoom
            )
        )
    }

    // --- Marker drag callbacks ---

    override fun onMarkerDrag(marker: Marker) {
        // not used
    }

    override fun onMarkerDragEnd(marker: Marker) {
        // Save updated position + current zoom
        location.lat = marker.position.latitude
        location.lng = marker.position.longitude
        location.zoom = map.cameraPosition.zoom
    }

    override fun onMarkerDragStart(marker: Marker) {
        // not used
    }
}
