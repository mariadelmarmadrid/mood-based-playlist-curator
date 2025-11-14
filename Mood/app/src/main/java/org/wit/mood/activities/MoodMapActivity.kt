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

class MoodMapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var binding: ActivityMoodMapBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMoodMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Find the map fragment from layout
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        // Center of SETU
        val setuMain = LatLng(52.245520, -7.138682)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(setuMain, 15f))

        // === REAL PLACES AROUND SETU ===

        // SETU Café
        map.addMarker(
            MarkerOptions()
                .position(LatLng(52.245657, -7.139112))
                .title("SETU Café ☕")
                .snippet("Nice place for a coffee or snack.")
        )

        // Arclabs Café
        map.addMarker(
            MarkerOptions()
                .position(LatLng(52.242953, -7.137268))
                .title("Arclabs Café ☕")
                .snippet("Quiet café next to the innovation hub.")
        )

        // Library
        map.addMarker(
            MarkerOptions()
                .position(LatLng(52.245505, -7.138104))
                .title("Library 📚")
                .snippet("Study, relax, or read.")
        )

        // Green area / small park
        map.addMarker(
            MarkerOptions()
                .position(LatLng(52.244912, -7.137653))
                .title("Campus Green 🌳")
                .snippet("Nice area to relax outdoors.")
        )

        // Browns Road Park
        map.addMarker(
            MarkerOptions()
                .position(LatLng(52.248879, -7.144149))
                .title("Browns Road Park 🌳")
                .snippet("Bigger park near SETU.")
        )

        // Harty's Takeaway
        map.addMarker(
            MarkerOptions()
                .position(LatLng(52.247666, -7.138710))
                .title("Harty’s Takeaway 🍔")
                .snippet("Popular student food spot.")
        )

        // Kingfisher Gym
        map.addMarker(
            MarkerOptions()
                .position(LatLng(52.247092, -7.143010))
                .title("Kingfisher Gym 🏋️‍♂️")
                .snippet("Great place to work out and destress.")
        )
    }


}
