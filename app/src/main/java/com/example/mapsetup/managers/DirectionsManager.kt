package com.example.mapsetup.managers

import com.example.mapsetup.api.DirectionsServiceApi
import android.content.Context
import android.graphics.Color
import android.util.Log
import com.example.mapsetup.R
import com.example.mapsetup.models.DirectionsResponse
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.google.maps.android.PolyUtil
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class DirectionsManager(private val context: Context, private val mapManager: MapManager) {

    private lateinit var directionsServiceApi: DirectionsServiceApi
    private val polylineList = mutableListOf<Polyline>()

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        directionsServiceApi = retrofit.create(DirectionsServiceApi::class.java)
    }

    fun getDirections(origin: LatLng, destination: LatLng) {
        val originStr = "${origin.latitude},${origin.longitude}"
        val destinationStr = "${destination.latitude},${destination.longitude}"
        val call = directionsServiceApi.getDirections(originStr, destinationStr, context.getString(R.string.MAPS_API_KEY))

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
                            val line=mapManager.drawPolyline(polyline,Color.BLACK,10f)
                            polylineList.add(line)

                        }else{
                            val line =mapManager.drawPolyline(polyline,Color.BLUE,5f)
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

    fun removeDirections() {
        polylineList.forEach { polyline ->
            polyline.remove()
        }
    }
}
