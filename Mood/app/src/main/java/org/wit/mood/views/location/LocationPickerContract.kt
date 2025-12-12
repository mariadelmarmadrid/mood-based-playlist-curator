package org.wit.mood.views.location

import com.google.android.gms.maps.model.LatLng
import org.wit.mood.models.Location

interface LocationPickerContract {
    interface View {
        fun enableZoomControls(enable: Boolean)
        fun moveCameraTo(latLng: LatLng, zoom: Float)
        fun showMarker(latLng: LatLng, title: String)
        fun setOnMapClickListener(listener: (LatLng) -> Unit)
        fun currentZoom(): Float
        fun finishWithOk(location: Location)
        fun finishCanceled()
    }
}
