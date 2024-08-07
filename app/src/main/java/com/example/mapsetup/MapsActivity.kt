package com.example.mapsetup

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
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

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, FeatureLayer.OnFeatureClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private var datasetLayer: FeatureLayer? = null
    var lastGlobalId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
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

        // Apply the style factory function to the feature layer.
        datasetLayer?.setFeatureStyle(styleFactory)
//        datasetLayer?.addOnFeatureClickListener { feature ->
//            // Extract details from the clicked feature
//            val featureId = feature.features[0].toString()
//
//
//            // Display the message using Toast
//            Toast.makeText(this, featureId, Toast.LENGTH_LONG).show()
//            Log.d("a",featureId)
//        }
    }

    override fun onFeatureClick(event: FeatureClickEvent) {
        // Get the dataset feature affected by the click.
        val clickFeatures: MutableList<Feature> = event.features
        lastGlobalId = null
        if (clickFeatures.get(0) is DatasetFeature) {
            lastGlobalId = ((clickFeatures.get(0) as DatasetFeature).getDatasetAttributes().get("stroke"))
            Toast.makeText(this, lastGlobalId.toString(), Toast.LENGTH_LONG).show()

        }
    }

}