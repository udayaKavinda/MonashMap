package com.example.mapsetup

import DirectionsService
import com.google.maps.android.PolyUtil
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import com.example.mapsetup.databinding.ActivityMapsBinding
import com.example.mapsetup.models.DirectionsResponse
import com.example.mapsetup.models.Polyline
import com.example.mapsetup.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.mapsetup.services.TrackingService
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
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, FeatureLayer.OnFeatureClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var destinationLatLng: LatLng
    private var originLatLng = LatLng(-37.911, 145.133)
    private lateinit var binding: ActivityMapsBinding
    private var datasetLayer: FeatureLayer? = null
    var lastGlobalId: String? = null
    private lateinit var popupWindow: PopupWindow
    private lateinit var popupTextView: TextView
    private lateinit var autoCompleteFragment: AutocompleteSupportFragment
    private var marker: Marker?=null
    private val polylineList = mutableListOf<com.google.android.gms.maps.model.Polyline>()
    private lateinit var directionsService: DirectionsService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //serach
        Places.initialize(applicationContext,"AIzaSyBF3z--DNmMn09CSsFm5T4I2EN6fCoVwx0")
        autoCompleteFragment=supportFragmentManager.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment
        autoCompleteFragment.setPlaceFields(listOf(Place.Field.ID,Place.Field.ADDRESS,Place.Field.LAT_LNG))
        val bounds = RectangularBounds.newInstance(
            LatLng(-37.9195, 145.1172),
            LatLng(-37.8965, 145.1524)
        )
        autoCompleteFragment.setLocationBias(bounds)
        autoCompleteFragment.setOnPlaceSelectedListener(object :PlaceSelectionListener{
            override fun onError(p0: Status) {
                Log.i("Error",p0.statusMessage.toString())
            }

            override fun onPlaceSelected(place: Place) {
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
        setupPopupWindow()

        binding.button.setOnClickListener {
            if(binding.button.text=="Cancel"){
                binding.button.visibility= View.GONE
                binding.button.text="Directions"
                marker?.remove()
                marker=null
                removeAllPolylines()
                return@setOnClickListener
            }
            drawPolyline()
            binding.button.text="Cancel"
        }
        sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        subscribeToObservers()

    }
    override fun onMapReady(googleMap: GoogleMap) {

        // Get the DATASET feature layer.
        mMap = googleMap
        // Add a marker in Sydney and move the camera
//        mMap.addMarker(MarkerOptions().position(monashCordinates).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(originLatLng,17F))
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
        mMap.setOnMapLongClickListener { latLng ->
            if(marker==null) {
                destinationLatLng = latLng
                marker = mMap.addMarker(
                    MarkerOptions().position(latLng)
                        .title("Marker at ${latLng.latitude}, ${latLng.longitude}")
                )
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18f))
                binding.button.visibility = View.VISIBLE
            }
        }
        styleDatasetsLayer()
        setupDirectionsService()
    }

    private fun styleDatasetsLayer() {

        // Create the style factory function.
        val styleFactory = FeatureLayer.StyleFactory { feature: Feature ->
            // Check if the feature is an instance of DatasetFeature.

            if (feature is DatasetFeature) {
                Log.d("aaa", feature.getDatasetAttributes().toString())


                return@StyleFactory FeatureStyle.Builder()
                    // Define a style with green fill at 50% opacity and
                    // solid green border.
//                    .fillColor(Integer.parseUnsignedInt(feature.getDatasetAttributes().get("fillColor").toString().drop(2),16).toInt())
                    .strokeColor(Integer.parseUnsignedInt(feature.getDatasetAttributes().get("stroke").toString().drop(2),16).toInt())
                    .strokeWidth(feature.getDatasetAttributes().get("stroke-width").toString().toFloat())
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

    }


    private fun sendCommandToService(action: String) {
        Intent(this, TrackingService::class.java).also {
            it.action = action
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



    fun setupDirectionsService(){
        val retrofit = Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        directionsService = retrofit.create(DirectionsService::class.java)

    }

    fun drawPolyline(){
        val origin = originLatLng.latitude.toString()+","+originLatLng.longitude.toString()
        val destination = destinationLatLng.latitude.toString()+","+destinationLatLng.longitude.toString()
        Toast.makeText(this, destination, Toast.LENGTH_LONG).show()
        val call = directionsService.getDirections(origin, destination, "AIzaSyBF3z--DNmMn09CSsFm5T4I2EN6fCoVwx0")

        call.enqueue(object : retrofit2.Callback<DirectionsResponse> {
            override fun onResponse(call: Call<DirectionsResponse>, response: retrofit2.Response<DirectionsResponse>) {
                if (response.isSuccessful) {
                    val directionsResponse = response.body()
//                    Log.d("MainActivityy", "Response:")
                    directionsResponse?.routes?.reversed()?.forEachIndexed { index, route ->
                        // Log the route summary or other details if needed
//                        Log.d("MainActivityy", "Route $index Summary: ${route}")

                        // Extract polyline points from each route
                        val polylinePoints = route.overview_polyline.points
                        val polyline = PolyUtil.decode(polylinePoints)

                        // Add the polyline to the map
                        if(index==directionsResponse.routes.size-1) {
                            val line=mMap.addPolyline(PolylineOptions().addAll(polyline).color(Color.BLACK).width(10f))
                            polylineList.add(line)

                        }else{
                            val line =mMap.addPolyline(PolylineOptions().addAll(polyline).color(Color.BLUE).width(5f))
                            polylineList.add(line)
                        }

                        }

                } else {
                    Log.e("MainActivity", "Error: ${response.errorBody()?.string()}")
                }
//                    directionsResponse?.routes?.firstOrNull()?.legs?.firstOrNull()?.steps?.forEach { step ->
//                        val polylinePoints = step.polyline.points
//                        val polyline = PolyUtil.decode(polylinePoints)
//                        mMap.addPolyline(PolylineOptions().addAll(polyline))
//                    }
//                } else {
//                    Log.e("MainActivityy", "Error: ${response.errorBody()?.string()}")
//                }
            }

            override fun onFailure(call: Call<DirectionsResponse>, t: Throwable) {
                Log.e("MainActivityy", "Failure: ${t.message}")
            }
        })

    }
    private fun removeAllPolylines() {
        polylineList.forEach { polyline ->
            polyline.remove()
        }
    }

}