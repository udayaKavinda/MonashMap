package com.example.mapsetup

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.mapsetup.databinding.ActivityMapsBinding
import com.google.android.gms.maps.model.DatasetFeature
import com.google.android.gms.maps.model.Feature
import com.google.android.gms.maps.model.FeatureClickEvent
import com.google.android.gms.maps.model.FeatureLayer
import com.google.android.gms.maps.model.FeatureLayerOptions
import com.google.android.gms.maps.model.FeatureStyle
import com.google.android.gms.maps.model.FeatureType
import com.google.android.gms.maps.model.LatLngBounds
import java.util.Random

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, FeatureLayer.OnFeatureClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private var datasetLayer: FeatureLayer? = null
    var lastGlobalId: String? = null
    private lateinit var popupWindow: PopupWindow
    private lateinit var popupTextView: TextView
    private val random = Random()
    private val handler = Handler(Looper.getMainLooper())
    private var updateRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        setupPopupWindow()

    }


    /**
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     */
    override fun onMapReady(googleMap: GoogleMap) {

        // Get the DATASET feature layer.
        mMap = googleMap
        // Add a marker in Sydney and move the camera
        val monashCordinates = LatLng(-37.911, 145.133)
//        mMap.addMarker(MarkerOptions().position(monashCordinates).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(monashCordinates,17F))
        mMap.setLatLngBoundsForCameraTarget(LatLngBounds(LatLng(-37.9195, 145.1172), LatLng(-37.8965, 145.1524)))
        mMap.setMinZoomPreference(15F)
        mMap.uiSettings.apply {
            isMyLocationButtonEnabled=true
        }
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ){
            mMap.isMyLocationEnabled=true
        }

        datasetLayer = mMap.getFeatureLayer(
            FeatureLayerOptions.Builder()
            .featureType(FeatureType.DATASET)
            // Specify the dataset ID.
            .datasetId("009fd42a-98a3-49f1-a54d-15371a561451")
            .build())

        // Apply style factory function to DATASET layer.
        datasetLayer?.addOnFeatureClickListener(this)
        styleDatasetsLayer()
    }

    private fun styleDatasetsLayer() {

        // Create the style factory function.
        val styleFactory = FeatureLayer.StyleFactory { feature: Feature ->
            // Check if the feature is an instance of DatasetFeature.
            if (feature is DatasetFeature) {

                return@StyleFactory FeatureStyle.Builder()
                    // Define a style with green fill at 50% opacity and
                    // solid green border.
                    .fillColor(0x8000ff00.toInt())
                    .strokeColor(0xff00ff00.toInt())
                    .strokeWidth(2F)
                    .build()
            }
            return@StyleFactory null
        }

        datasetLayer?.setFeatureStyle(styleFactory)

    }

    override fun onFeatureClick(event: FeatureClickEvent) {
        // Get the dataset feature affected by the click.
        val clickFeatures: MutableList<Feature> = event.features
        lastGlobalId = null
        if (clickFeatures.get(0) is DatasetFeature) {
            lastGlobalId = ((clickFeatures.get(0) as DatasetFeature).getDatasetAttributes().get("stroke"))
            showPopupWindow()

        }
    }

    private fun setupPopupWindow() {
        // Inflate the popup window layout
        val inflater = LayoutInflater.from(this)
        val popupView = inflater.inflate(R.layout.popup_window, null)
        popupTextView = popupView.findViewById(R.id.popup_text)
        // Create the PopupWindow
        popupWindow = PopupWindow(popupView,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            isFocusable = true // Allows interaction with the PopupWindow
            isOutsideTouchable = true // Dismiss the window when touching outside
            setBackgroundDrawable(null) // Optional: set a background to make it look better
        }
    }

    private fun showPopupWindow() {
        // Show the PopupWindow
        popupWindow.showAtLocation(binding.root, android.view.Gravity.CENTER, 0, 0)
//        Toast.makeText(this, lastGlobalId.toString(), Toast.LENGTH_LONG).show()
        startUpdatingPopupText()

    }

    private fun startUpdatingPopupText() {
        // Stop any previous updates if running
        stopUpdatingPopupText()

        // Define a Runnable to update the text every second
        updateRunnable = object : Runnable {
            override fun run() {
                // Generate a random number and update the text view
                val randomNumber = random.nextInt(100) // Random number between 0 and 99
                popupTextView.text = lastGlobalId.toString()+randomNumber.toString()

                // Schedule the next update
                handler.postDelayed(this, 1000) // Update every second
            }
        }

        // Start the initial update
        handler.post(updateRunnable!!)
    }

    private fun stopUpdatingPopupText() {
        // Remove any pending updates
        updateRunnable?.let {
            handler.removeCallbacks(it)
        }
    }
}