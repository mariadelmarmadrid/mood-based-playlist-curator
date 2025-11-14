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
    }


    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        // Example mood spots (you can use Waterford / SETU coordinates etc.)
        val happyCafe = LatLng(52.2457, -7.1391)   // example
        val relaxedPark = LatLng(52.2460, -7.1400)
        val sadCinema = LatLng(52.2448, -7.1385)
        val angryGym = LatLng(52.2452, -7.1410)

        map.addMarker(
            MarkerOptions()
                .position(happyCafe)
                .title("Happy place: Café ☕")
                .snippet("Great coffee, cozy vibes")
        )
        map.addMarker(
            MarkerOptions()
                .position(relaxedPark)
                .title("Relaxed place: Park 🌳")
                .snippet("Nice for a walk and fresh air")
        )
        map.addMarker(
            MarkerOptions()
                .position(sadCinema)
                .title("Sad movie spot 🎬")
                .snippet("Perfect for crying at a movie 😅")
        )
        map.addMarker(
            MarkerOptions()
                .position(angryGym)
                .title("Angry outlet: Gym 💪")
                .snippet("Punch the stress out")
        )

        // Center camera roughly over the area
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(happyCafe, 15f))
    }
}
