package org.wit.mood.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import org.wit.mood.R
import org.wit.mood.models.Location

class MoodLocationPickerActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private var marker: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mood_location_picker)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Handle system back button to return selected location
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    marker?.let {
                        val loc = Location(
                            lat = it.position.latitude,
                            lng = it.position.longitude,
                            zoom = map.cameraPosition.zoom
                        )

                        setResult(
                            Activity.RESULT_OK,
                            Intent().putExtra("location", loc)
                        )
                    }
                    finish()
                }
            }
        )
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        val default = LatLng(52.2460, -7.1390) // SETU default
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(default, 15f))

        map.setOnMapClickListener { pos ->
            marker?.remove()
            marker = map.addMarker(
                MarkerOptions()
                    .position(pos)
                    .title("Selected location")
            )
        }
    }
}
