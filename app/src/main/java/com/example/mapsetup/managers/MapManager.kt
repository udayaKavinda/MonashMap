package com.example.mapsetup.managers

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import android.widget.ImageButton
import androidx.core.content.ContextCompat
import com.example.mapsetup.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions

class MapManager(private val context: Context, private val rootView: View, private val googleMap: GoogleMap) {

    private val originLatLng = LatLng(-37.911, 145.133)
    private var destinationMaker: Marker? = null

    init {
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(originLatLng,17F))
        googleMap.setLatLngBoundsForCameraTarget(LatLngBounds(LatLng(-37.9195, 145.1172), LatLng(-37.8965, 145.1524)))
        googleMap.setMinZoomPreference(15F)
        addHazardMarkers(LatLng(-37.907949, 145.137300))
        googleMap.uiSettings.apply {
            isMyLocationButtonEnabled = false
        }
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {
            googleMap.isMyLocationEnabled = true
        }


        setUpMyLocationButton(rootView.findViewById(R.id.my_location_button))
    }


    fun addHazardMarkers(location: LatLng) {

        val drawable = ContextCompat.getDrawable(context, R.drawable.hazard)!!
        val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        // Resize the bitmap
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 100, 100, false) // 100x100 pixels
        addMarker(location, "Flood", resizedBitmap)

    }

    fun drawPolyline(points: List<LatLng>, color: Int, width: Float): Polyline {
        return googleMap.addPolyline(PolylineOptions().addAll(points).color(color).width(width))
    }

    fun addMarker(location: LatLng, title: String, icon: Bitmap? = null): Marker {
        val markerOptions = MarkerOptions().position(location).title(title)
        icon?.let { markerOptions.icon(BitmapDescriptorFactory.fromBitmap(it)) }
        return googleMap.addMarker(markerOptions)!!
    }

    fun addDestinationMarker(location: LatLng) {
        destinationMaker=addMarker(location,"Destination")
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 18f))
    }

    fun removeDestinationMarker() {
        destinationMaker?.remove()
        destinationMaker = null
    }

    fun setUpMyLocationButton(button: ImageButton) {
        button.setOnClickListener {
            val locationResult = googleMap.myLocation
            locationResult?.let {
                val currentLatLng = LatLng(it.latitude, it.longitude)
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 17F))
            }
        }
    }

}