package com.example.mapsetup

import android.os.Bundle
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.mapsetup.databinding.ActivityDataVisualizeBinding
import com.example.mapsetup.services.SensorService
import com.example.mapsetup.services.TrackingService
import com.example.mapsetup.services.WeatherService

class DataVisualize : AppCompatActivity() {

    private lateinit var binding: ActivityDataVisualizeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDataVisualizeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        subscribeToObservers()
    }

    private fun subscribeToObservers() {
        TrackingService.pathPoints.observe(this, Observer { pathPoint ->
            pathPoint?.let {
                binding.tvTrackingData.text = "Lat: ${it.latitude}, Lon: ${it.longitude}\n"
            }
        })

        SensorService.accelerometerData.observe(this, Observer { accelerometer ->
            accelerometer?.let {
                binding.tvAccelerometerData.text = "accelerometer \n X: ${it.x}, Y: ${it.y}, Z: ${it.z}\n"
            }
        })

        SensorService.gyroscopeData.observe(this, Observer { gyroscope ->
            gyroscope?.let {
                binding.tvGyroscopeData.text = "gyroscope \n X: ${it.x}, Y: ${it.y}, Z: ${it.z}\n"
            }
        })

        SensorService.magnetometerData.observe(this, Observer { magnetometer ->
            magnetometer?.let {
                binding.tvMagnetometerData.text = "magnetometer \n X: ${it.x}, Y: ${it.y}, Z: ${it.z}\n"
            }
        })

        WeatherService.weatherData.observe(this, Observer { weather ->
            weather?.let {
                binding.tvWeatherData.text = "Temp: ${it.current.temperature_2m}, Humidity: ${it.current.relative_humidity_2m}"
            }
        })
    }
}
