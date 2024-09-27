package com.example.mapsetup

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.example.mapsetup.databinding.ActivityMapsBinding
import com.example.mapsetup.managers.DirectionsManager
import com.example.mapsetup.managers.FeatureLayerManager
import com.example.mapsetup.managers.MapManager
import com.example.mapsetup.managers.PopupManager
import com.example.mapsetup.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.mapsetup.other.MapsActivityState
import com.example.mapsetup.services.BluetoothService
import com.example.mapsetup.services.SensorService
import com.example.mapsetup.services.TrackingService
import com.example.mapsetup.services.WeatherService
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.DatasetFeature
import com.google.android.gms.maps.model.Feature
import com.google.android.gms.maps.model.FeatureClickEvent
import com.google.android.gms.maps.model.FeatureLayer
import com.google.android.gms.maps.model.FeatureLayerOptions
import com.google.android.gms.maps.model.FeatureStyle
import com.google.android.gms.maps.model.FeatureType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener


class MapsActivity : AppCompatActivity(), OnMapReadyCallback{

    private lateinit var directionsManager: DirectionsManager
    private lateinit var mapManager: MapManager
    private lateinit var featureLayerManager: FeatureLayerManager
    private lateinit var mapsActivityState: MapsActivityState

    private lateinit var mMap: GoogleMap

    private lateinit var destinationLatLng: LatLng
    private var originLatLng = LatLng(-37.911, 145.133)

    private lateinit var binding: ActivityMapsBinding
    private lateinit var autoCompleteFragment: AutocompleteSupportFragment
    private var marker: Marker?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mapsActivityState=MapsActivityState.IDEAL

        Places.initialize(applicationContext,"AIzaSyBF3z--DNmMn09CSsFm5T4I2EN6fCoVwx0")
        autoCompleteFragment=supportFragmentManager.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment
        autoCompleteFragment.setPlaceFields(listOf(Place.Field.ID,Place.Field.ADDRESS,Place.Field.LAT_LNG))
        val bounds = RectangularBounds.newInstance(
            LatLng(-37.9195, 145.1172),
            LatLng(-37.8965, 145.1524)
        )
        autoCompleteFragment.setLocationRestriction(bounds)
        autoCompleteFragment.setHint("Search here");
        autoCompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onError(p0: Status) {
                Log.i("Error",p0.statusMessage.toString())
            }

            override fun onPlaceSelected(place: Place) {
                autoCompleteFragment.setHint(place.address?.toString() ?: "Search here")
//                Log.i("asd", place.address?.toString() ?: "s")
                marker?.remove()
                destinationLatLng=place.latLng
                val newLatLngZoom=CameraUpdateFactory.newLatLngZoom(destinationLatLng,18F)
                marker=mMap.addMarker(MarkerOptions().position(destinationLatLng).title("Destinatiom"))
                mMap.animateCamera(newLatLngZoom)
                binding.button.visibility= View.VISIBLE
            }

        })


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


        binding.button.setOnClickListener {
            if(mapsActivityState==MapsActivityState.DESTINATION_SET){
                mapsActivityState=MapsActivityState.DIRECTIONS_START
                directionsManager.getDirections(originLatLng,destinationLatLng)
                binding.button.text="Cancel"
            }else if(mapsActivityState==MapsActivityState.DIRECTIONS_START){
                mapsActivityState=MapsActivityState.IDEAL
                binding.button.text="Directions"
                binding.button.visibility= View.GONE
                directionsManager.removeDirections()
                mapManager.removeDestinationMarker()
            }
        }
        sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        startWeatherService()
        startSensorService()
        subscribeToObservers()
        startLocationService()
    }
    override fun onMapReady(googleMap: GoogleMap) {



        // Get the DATASET feature layer.
        mMap = googleMap
        mapManager = MapManager(this,binding.root, mMap)
        directionsManager = DirectionsManager(this, mapManager)
        featureLayerManager= FeatureLayerManager(this,binding.root,mMap)



        mMap.setOnMapLongClickListener { latLng ->
            if(mapsActivityState==MapsActivityState.IDEAL){
                mapsActivityState=MapsActivityState.DESTINATION_SET
                destinationLatLng=latLng
                mapManager.addDestinationMarker(latLng)
                binding.button.visibility = View.VISIBLE

            }else if(mapsActivityState==MapsActivityState.DESTINATION_SET){
                destinationLatLng=latLng
                mapManager.removeDestinationMarker()
                mapManager.addDestinationMarker(latLng)
            }

        }
    }

    private fun sendCommandToService(action: String) {
        Intent(this, TrackingService::class.java).also {
            it.action = action
            startService(it)
        }
    }
    private fun startWeatherService() {
        Intent(this, WeatherService::class.java).also {
            startService(it)
        }
    }

    private fun startLocationService() {
        Intent(this, BluetoothService::class.java).also {
            startService(it)
        }
    }

    private fun startSensorService() {
        Intent(this, SensorService::class.java).also {
            startService(it)
        }
    }

    private fun subscribeToObservers() {
        TrackingService.pathPoints.observe(this, Observer { pathPoint ->
            // Update UI based on pathPoints
            pathPoint?.let {
                originLatLng=it
                // Handle new location
//                Log.d("MainActivity", "Path Point: $it")
            }
        })
    }

//    private fun updateUI(){
//        if(mapsActivityState==MapsActivityState.IDEAL){
//            binding.button.visibility= View.VISIBLE
//
//        }else if(mapsActivityState==MapsActivityState.DESTINATION_SET){
//
//            binding.button.visibility= View.GONE
//        }else if(mapsActivityState==MapsActivityState.DIRECTIONS_START){
//
//
//        }
//    }






}