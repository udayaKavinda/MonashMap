package com.example.mapsetup.services

import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.LifecycleService
import com.example.mapsetup.notifications.NotificationHelper
import com.example.mapsetup.other.Constants
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import androidx.lifecycle.MutableLiveData
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager

class TrackingService : LifecycleService() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var notificationHelper: NotificationHelper
    private var isFirstRun = true

    companion object {
        var isTracking = MutableLiveData<Boolean>()
        var pathPoints = MutableLiveData<LatLng>()
    }

    override fun onCreate() {
        super.onCreate()
        notificationHelper = NotificationHelper(this)
        startForeground(Constants.NOTIFICATION_ID, notificationHelper.getNotification())
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                Constants.ACTION_START_OR_RESUME_SERVICE -> {
                    if (isFirstRun) {
                        startLocationUpdates()
                        isFirstRun = false
                        isTracking.postValue(true)
                    }
                    Log.d("TrackingService", "Service started or resumed")
                }
                Constants.ACTION_PAUSE_SERVICE -> {
                    Log.d("TrackingService", "Service paused")
                }
                Constants.ACTION_STOP_SERVICE -> {
                    stopSelf()
                    Log.d("TrackingService", "Service stopped")
                }

                else -> {}
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            locationResult?.let {
                val location = it.lastLocation
                location?.let {
                    pathPoints.postValue(LatLng(location.latitude, location.longitude))
                }
            }
        }
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            interval = 1000
            fastestInterval = 500
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
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

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }
}
