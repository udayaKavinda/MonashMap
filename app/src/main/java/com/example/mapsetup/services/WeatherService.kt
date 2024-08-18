package com.example.mapsetup.services
import com.example.mapsetup.api.WeatherServiceApi
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.Observer
import com.example.mapsetup.models.WeatherResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class WeatherService : LifecycleService() {
    private lateinit var retrofit: Retrofit
    private lateinit var weatherServiceApi: WeatherServiceApi
    private val TAG = "WeatherService"
    private val BASE_URL = "https://api.open-meteo.com/"
    private lateinit var scheduler: ScheduledExecutorService
    private var currentLatitude:Double=-37.9195
    private var currentLongitude:Double=145.1172


    override fun onCreate() {
        super.onCreate()
        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        weatherServiceApi = retrofit.create(WeatherServiceApi::class.java)
        subscribeToObservers()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!::scheduler.isInitialized) {
            Log.d(TAG, "Scheduler initialized")
            scheduler = Executors.newScheduledThreadPool(1)
            scheduler.scheduleAtFixedRate({
                fetchWeatherData(currentLatitude, currentLongitude)
            }, 0, 6, TimeUnit.SECONDS)
        }
        return  super.onStartCommand(intent, flags, startId) // Keep the service running
    }

    private fun fetchWeatherData(latitude: Double, longitude: Double) {
        val call = weatherServiceApi.getCurrentWeather(
            latitude,
            longitude,
            "temperature_2m,relative_humidity_2m,apparent_temperature,precipitation,cloud_cover,surface_pressure,wind_speed_10m,wind_direction_10m",
            "auto"
        )
        call.enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                if (response.isSuccessful) {
                    val weatherResponse = response.body()
                    handleWeatherData(weatherResponse)
                } else {
                    handleWeatherDataError(Exception("Request failed with status code: ${response.code()}"))
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                handleWeatherDataError(t)
            }
        })
    }

    private fun handleWeatherData(weatherResponse: WeatherResponse?) {
        Log.d(TAG, "Weather data received: $weatherResponse")
        // Process weather data or send it back to an Activity via BroadcastReceiver if needed
    }

    private fun subscribeToObservers() {
        TrackingService.pathPoints.observe(this, Observer { pathPoint ->
            // Update UI based on pathPoints
            pathPoint?.let {
                currentLatitude=it.latitude
                currentLongitude=it.longitude
                // Handle new location
//                Log.d("WeatherService", "Path Point: $it")
            }
        })
    }

    private fun handleWeatherDataError(throwable: Throwable) {
        Log.e(TAG, "Weather data fetch error", throwable)
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        scheduler.shutdown() // Shut down the scheduler
    }
}
