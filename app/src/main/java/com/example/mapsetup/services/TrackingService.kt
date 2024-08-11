package com.example.mapsetup.services

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import com.example.mapsetup.other.Constants
import com.example.mapsetup.other.Constants.ACTION_PAUSE_SERVICE
import com.example.mapsetup.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.mapsetup.other.Constants.ACTION_STOP_SERVICE
import com.example.mapsetup.other.Constants.NOTIFICATION_CHANNEL_ID
import com.example.mapsetup.other.Constants.NOTIFICATION_CHANNEL_NAME
import com.example.mapsetup.other.Constants.NOTIFICATION_ID
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng

class TrackingService : LifecycleService() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var isFirstRun = true

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }

    companion object{
        var isTracking= MutableLiveData<Boolean>()
        var pathPoints=MutableLiveData<LatLng>()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_START_OR_RESUME_SERVICE -> {
                    if (isFirstRun) {
                        startForegroundService()
                        startLocationUpdates()
                        isFirstRun = false
                        isTracking.postValue(true)
                    }
                    Log.d("TrackingService", "Service started or resumed")
                }
                ACTION_PAUSE_SERVICE -> {
                    // Implement pause functionality if needed
                    Log.d("TrackingService", "Service paused")
                }
                ACTION_STOP_SERVICE -> {
                    // Implement stop functionality if needed
                    stopSelf() // Stop the service
                    Log.d("TrackingService", "Service stopped")
                }
                else -> {
                    // Handle other actions if needed
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startForegroundService() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            createNotificationChannel(notificationManager)

        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(false)
            .setOngoing(true)
            .setSmallIcon(android.R.drawable.ic_delete) // Change this to your app's icon
            .setContentTitle("Running App")
            .setContentText("00:00:00")

        startForeground(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            locationResult?.let {
                val location = it.lastLocation
                if (location != null) {
//                    Log.d("TrackingService", "Location: ${location.latitude}, ${location.longitude}")
                    pathPoints.postValue(LatLng(location.latitude, location.longitude))
                }
            }
        }
    }
    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            interval = 1000 // Update interval in milliseconds
            fastestInterval = 500 // Fastest update interval in milliseconds
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onDestroy() {
        stopLocationUpdates()
        super.onDestroy()
    }
}
