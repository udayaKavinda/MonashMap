package com.example.mapsetup.services

import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.mapsetup.notifications.NotificationHelper
import com.example.mapsetup.api.WeatherServiceApi
import com.example.mapsetup.models.WeatherResponse
import com.example.mapsetup.other.Constants
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
    private lateinit var notificationHelper: NotificationHelper
    private val TAG = "WeatherService"
    private val BASE_URL = "https://api.open-meteo.com/"
    private lateinit var scheduler: ScheduledExecutorService
    private var currentLatitude: Double = -37.9195
    private var currentLongitude: Double = 145.1172

    companion object {
        var weatherData = MutableLiveData<WeatherResponse>()
    }

    override fun onCreate() {
        super.onCreate()
        notificationHelper = NotificationHelper(this)
        startForeground(Constants.NOTIFICATION_ID, notificationHelper.getNotification())

        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        weatherServiceApi = retrofit.create(WeatherServiceApi::class.java)
        subscribeToObservers()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!::scheduler.isInitialized) {
            scheduler = Executors.newScheduledThreadPool(1)
            scheduler.scheduleAtFixedRate({
                fetchWeatherData(currentLatitude, currentLongitude)
            }, 0, 6, TimeUnit.SECONDS)
        }
        return super.onStartCommand(intent, flags, startId)
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
        weatherResponse?.let {
            weatherData.postValue(it)
//            Log.d(TAG, "Weather data received: $it")
        }
    }

    private fun subscribeToObservers() {
        TrackingService.pathPoints.observe(this, Observer { pathPoint ->
            pathPoint?.let {
                currentLatitude = it.latitude
                currentLongitude = it.longitude
            }
        })
    }

    private fun handleWeatherDataError(throwable: Throwable) {
        Log.e(TAG, "Weather data fetch error", throwable)
    }

    override fun onDestroy() {
        scheduler.shutdown()
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }
}
