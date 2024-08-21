package com.example.mapsetup.services

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import com.example.mapsetup.models.Acceleromrter
import com.example.mapsetup.models.Gyroscope
import com.example.mapsetup.models.Magnetometer
import com.example.mapsetup.notifications.NotificationHelper
import com.example.mapsetup.other.Constants.NOTIFICATION_ID

class SensorService : LifecycleService(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var gyroscope: Sensor? = null
    private var magnetometer: Sensor? = null

    private var lastAccelerometerValues: FloatArray? = null
    private var lastGyroscopeValues: FloatArray? = null
    private var lastMagnetometerValues: FloatArray? = null
    private lateinit var notificationHelper: NotificationHelper

    companion object {
        var accelerometerData = MutableLiveData<Acceleromrter>()
        var magnetometerData = MutableLiveData<Magnetometer>()
        var gyroscopeData = MutableLiveData<Gyroscope>()
    }

    override fun onCreate() {
        super.onCreate()
        notificationHelper = NotificationHelper(this)
        startForeground(NOTIFICATION_ID, notificationHelper.getNotification())

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        // Initialize sensors
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        startLocationUpdates()
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Start the sensor monitoring when the service is started
        intent?.let {
            // Handle actions if needed
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startLocationUpdates() {
        accelerometer?.also { acc ->
            sensorManager.registerListener(this, acc, SensorManager.SENSOR_DELAY_NORMAL)
        }
        gyroscope?.also { gyro ->
            sensorManager.registerListener(this, gyro, SensorManager.SENSOR_DELAY_NORMAL)
        }
        magnetometer?.also { mag ->
            sensorManager.registerListener(this, mag, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    private fun stopLocationUpdates() {
        sensorManager.unregisterListener(this)
    }


    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            when (it.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> handleSensorEvent(it.sensor.type, it.values, lastAccelerometerValues) { lastAccelerometerValues = it }
                Sensor.TYPE_GYROSCOPE -> handleSensorEvent(it.sensor.type, it.values, lastGyroscopeValues) { lastGyroscopeValues = it }
                Sensor.TYPE_MAGNETIC_FIELD -> handleSensorEvent(it.sensor.type, it.values, lastMagnetometerValues) { lastMagnetometerValues = it }
            }
        }
    }

    private fun handleSensorEvent(sensor: Int, currentValues: FloatArray, lastValues: FloatArray?, updateLastValues: (FloatArray) -> Unit) {
        if (lastValues == null || hasSignificantChange(sensor, lastValues, currentValues)) {
            updateLastValues(currentValues.clone())
            var name = ""
            when (sensor) {
                Sensor.TYPE_ACCELEROMETER -> {
                    name = "ACCELEROMETER"
                    accelerometerData.postValue(Acceleromrter(currentValues[0], currentValues[1], currentValues[2]))
                }
                Sensor.TYPE_GYROSCOPE -> {
                    name = "GYROSCOPE"
                    gyroscopeData.postValue(Gyroscope(currentValues[0], currentValues[1], currentValues[2]))
                }
                Sensor.TYPE_MAGNETIC_FIELD -> {
                    name = "MAGNETIC_FIELD"
                    magnetometerData.postValue(Magnetometer(currentValues[0], currentValues[1], currentValues[2]))
                }
            }
//            Log.d("SensorService$name", ": x=${currentValues[0]}, y=${currentValues[1]}, z=${currentValues[2]}")
        }
    }

    private fun hasSignificantChange(sensor: Int, lastValues: FloatArray?, currentValues: FloatArray): Boolean {
        var threshold: Float = 0f
        when (sensor) {
            Sensor.TYPE_ACCELEROMETER -> {
                threshold = 0.1f
            }
            Sensor.TYPE_GYROSCOPE -> {
                threshold = 0.01f
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                threshold = 1f
            }
        }

        // Ensure lastValues is not null before proceeding
        if (lastValues == null) return true

        for (i in currentValues.indices) {
            if (Math.abs(lastValues[i] - currentValues[i]) > threshold) {
                return true
            }
        }
        return false
    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Handle changes in sensor accuracy if needed
    }

    override fun onDestroy() {
        stopLocationUpdates()
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }
}
