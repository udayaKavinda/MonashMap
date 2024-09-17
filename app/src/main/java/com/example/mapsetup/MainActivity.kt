package com.example.mapsetup

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.mapsetup.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val REQUEST_PERMISSIONS = 4
    private lateinit var mapsButton: Button
    private lateinit var smartShoeButton: Button
    private lateinit var dataButton: Button
    private lateinit var allowPermissions: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mapsButton = findViewById(R.id.maps_button)
        smartShoeButton = findViewById(R.id.smart_shoe_button)
        dataButton = findViewById(R.id.data_visalize)
        allowPermissions=findViewById(R.id.allow_permissions)

        mapsButton.setOnClickListener {
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
        }
        smartShoeButton.setOnClickListener {
            // Add your functionality here
        }
        dataButton.setOnClickListener {
            val intent = Intent(this, Bluetooth::class.java)
            startActivity(intent)
        }
        uiHandler()
        requestPermissions()
    }

    private fun isLocationPermissionGranted(): Boolean {
        return (ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED)
    }
    private fun isNotificationPermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun isBluetoothPermissionsGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSIONS) {
            for (i in permissions.indices) {
                when (permissions[i]) {
                    Manifest.permission.ACCESS_FINE_LOCATION -> {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, "Location permission granted", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
                        }
                    }
                    Manifest.permission.POST_NOTIFICATIONS -> {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
                        }
                    }
                    Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT -> {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, "Bluetooth permission granted", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Bluetooth permission denied", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            uiHandler()
        }
    }


    private  fun uiHandler() {
        if (isLocationPermissionGranted() && isNotificationPermissionGranted() && isBluetoothPermissionsGranted()) {
            mapsButton.visibility = View.VISIBLE
            smartShoeButton.visibility = View.VISIBLE
            dataButton.visibility = View.VISIBLE
            allowPermissions.visibility=View.GONE
        }else{
            allowPermissions.visibility=View.VISIBLE
        }
    }

    private fun requestPermissions() {
        val permissions = mutableListOf<String>()

        if (!isLocationPermissionGranted()) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (!isNotificationPermissionGranted()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        if (!isBluetoothPermissionsGranted()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                permissions.add(Manifest.permission.BLUETOOTH_SCAN)
                permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
            } else {
                permissions.add(Manifest.permission.BLUETOOTH)
                permissions.add(Manifest.permission.BLUETOOTH_ADMIN)
            }
        }

        if (permissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissions.toTypedArray(),
                REQUEST_PERMISSIONS
            )
        }
    }
}



