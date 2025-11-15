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

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        // -------------------------------
        // 1. HAPPY LOCATIONS 😊
        // -------------------------------
        val happyMarkers = listOf(
            Triple(
                LatLng(52.246003781438574, -7.138020258486807),
                "Happy place: Centra Viking Food Hall ☕",
                "Bright, social atmosphere — good for positive energy"
            ),
            Triple(
                LatLng(52.24531520331995, -7.140800329642434),
                "Happy place: Student Hub 🎉",
                "Lively space where you naturally feel upbeat"
            )
        )

        // -------------------------------
        // 2. RELAXED LOCATIONS 😌
        // -------------------------------
        val relaxedMarkers = listOf(
            Triple(
                LatLng(52.24072650189237, -7.124451987984857),
                "Relaxed place: Waterford Nature Park 🌿",
                "Open green space perfect for slowing down and breathing"
            ),
            Triple(
                LatLng(52.27001966241276, -7.1375516589339965),
                "Relaxed place: Greenway Trail Start \uD83D\uDEB6\u200D♀\uFE0F",
                "Great starting point for a relaxing walk surrounded by nature"
            )
        )

        // -------------------------------
        // 3. NEUTRAL LOCATIONS 😐
        // -------------------------------
        val neutralMarkers = listOf(
            Triple(
                LatLng(52.24476934377214, -7.142239168927888),
                "Neutral spot: Fuel Station ⛽",
                "A routine stop — fuel, snacks, or just passing through"
            ),
            Triple(
                LatLng(52.24508427507819, -7.137684557067292),
                "Neutral spot: Bus Stop 🚏",
                "Simple everyday location where moods tend to be neutral"
            )
        )

        // -------------------------------
        // 4. SAD LOCATIONS 😢
        // -------------------------------
        val sadMarkers = listOf(
            Triple(
                LatLng(52.24535559769644, -7.138168843282043),
                "Sad place: Library 📚",
                "Quiet place to clear your head during stressful exam weeks."
            ),
            Triple(
                LatLng(52.24536423679305, -7.139492894570469),
                "Sad place: Empty Carpark Corner 🅿️",
                "Lonely, grey environment fitting a sad mood"
            )
        )

        // -------------------------------
        // 5. ANGRY LOCATIONS 😠
        // -------------------------------
        val angryMarkers = listOf(
            Triple(
                LatLng(52.24581511854891, -7.137333464540997),
                "Angry outlet: Exam Hall \uD83D\uDE20",
                "Exam stress levels: 100/100. The place where anger and panic meet"
            ),
            Triple(
                LatLng(52.24553667441552, -7.140606011268003),
                "Angry outlet: Sports Hall ⚽",
                "Active environment where frustration can be turned into movement"
            )
        )

        // ---------------------------------
        // Add ALL mood-grouped markers
        // ---------------------------------

        fun addMarkerList(list: List<Triple<LatLng, String, String>>) {
            list.forEach { (pos, title, desc) ->
                map.addMarker(
                    MarkerOptions()
                        .position(pos)
                        .title(title)
                        .snippet(desc)
                )
            }
        }

        addMarkerList(happyMarkers)
        addMarkerList(relaxedMarkers)
        addMarkerList(neutralMarkers)
        addMarkerList(sadMarkers)
        addMarkerList(angryMarkers)

        // Center map over SETU Arena Café
        map.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(52.24645, -7.13890),
                16f
            )
        )
    }
}
