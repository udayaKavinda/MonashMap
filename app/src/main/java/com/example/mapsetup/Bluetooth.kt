package com.example.mapsetup

import android.Manifest
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import org.json.JSONObject
import java.util.UUID
import java.util.stream.Collector.Characteristics

class Bluetooth : AppCompatActivity() {
    var characteristic2: BluetoothGattCharacteristic? = null
    var ggatt:BluetoothGatt?=null

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private val bluetoothLeScanner by lazy {
        bluetoothAdapter?.bluetoothLeScanner
    }

    private var bluetoothGatt: BluetoothGatt? = null

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                gatt?.requestMtu(156)
                Log.i("ScanCallback", "Connected to GATT server.")
                Log.i("ScanCallback", "Attempting to start service discovery: ${gatt?.discoverServices()}")
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i("ScanCallback", "Disconnected from GATT server.")
            }
        }

//        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
//            super.onServicesDiscovered(gatt, status)
//            if (status == BluetoothGatt.GATT_SUCCESS) {
//                Log.i("ScanCallback", "Services discovered: ${gatt?.services}")
//                val service = gatt?.getService(UUID.fromString("00000000-cc7a-482a-984a-7f2ed5b3e58f"))
//                val characteristic2 = service?.getCharacteristic(UUID.fromString("00000000-8e22-4541-9d4c-21edae82ed19"))
//                val characteristic= service?.getCharacteristic(UUID.fromString("00000001-8e22-4541-9d4c-21edae82ed19"))
//
//                characteristic2?.setValue(byteArrayOf(0x01)) // Example value
//                gatt?.writeCharacteristic(characteristic2)
//
//                gatt?.setCharacteristicNotification(characteristic, true)
//                val descriptor = characteristic?.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
//                descriptor?.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
//                gatt?.writeDescriptor(descriptor)
//
//
////                Log.i("ScanCallback", characteristic2?.descriptors?.get(0)?.uuid.toString())
////                gatt?.services?.forEach { service ->
////                    Log.i("ScanCallback", "Service uuids: ${service.uuid}")
////                    if (service.uuid.toString() == "00000000-cc7a-482a-984a-7f2ed5b3e58f") {
////                        service.characteristics.forEach { characteristic ->
////                            Log.i("ScanCallback", "Characteristic uuids: ${characteristic.uuid}")
////                        }
////                    }
////                }
//                // Interact with the services here
//            } else {
//                Log.w("ScanCallback", "onServicesDiscovered received: $status")
//            }
//        }

        override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
            super.onMtuChanged(gatt, mtu, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i("ScanCallback", "MTU size changed to $mtu")
                gatt?.discoverServices()
            } else {
                Log.e("ScanCallback", "Failed to change MTU size")
            }
        }
override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
    if (status == BluetoothGatt.GATT_SUCCESS) {
        ggatt=gatt
        val service = gatt?.getService(UUID.fromString("00000000-cc7a-482a-984a-7f2ed5b3e58f"))
        val characteristic = service?.getCharacteristic(UUID.fromString("00000001-8e22-4541-9d4c-21edae82ed19"))
        characteristic2= service?.getCharacteristic(UUID.fromString("00000000-8e22-4541-9d4c-21edae82ed19"))
//        ggatt?.requestMtu(156)


         // Example value
        if (characteristic != null) {
            val notificationEnabled = gatt.setCharacteristicNotification(characteristic, true)
            if (notificationEnabled) {
                Log.i("ScanCallback", "Notifications enabled for ${characteristic.uuid}")
                val descriptor = characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
                descriptor?.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                val descriptorWriteSuccess = gatt.writeDescriptor(descriptor)
                Log.i("ScanCallback", "Descriptor write initiated: $descriptorWriteSuccess")
            } else {
                Log.e("ScanCallback", "Failed to enable notifications")
            }
        } else {
            Log.e("ScanCallback", "Characteristic not found")
        }
    } else {
        Log.w("ScanCallback", "onServicesDiscovered received: $status")
    }
}
        override fun onCharacteristicRead(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                characteristic?.value?.let {
                    Log.i("ScanCallback", "Characteristic read: ${it.joinToString()}")
                }
            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i("ScanCallback", "Characteristic written successfully")
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?
        ) {
            characteristic?.value?.let {
                Log.i("ScanCallback", String(it, Charsets.UTF_8)+it.size.toString())
//                Log.i("ScanCallback" )
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bluetooth)

        requestBluetoothPermissions()
        startScanning()
        val sendButton = findViewById<Button>(R.id.send_button)
        sendButton.setOnClickListener {
            characteristic2?.setValue(byteArrayOf(0x01))
            ggatt?.writeCharacteristic(characteristic2)

        }

    }

    private fun startScanning() {
        val scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                super.onScanResult(callbackType, result)
                result?.let {
                    val device = it.device
                    Log.i("ScanCallback", "Device found: ${device.name} - ${device.address}")
                    if (device.name == "MonashMap") {
                        // Stop scanning and connect to the device
                        bluetoothLeScanner?.stopScan(this)
                        connectToDevice(device)
                    }
                }
            }

            override fun onBatchScanResults(results: MutableList<ScanResult>?) {
                super.onBatchScanResults(results)
            }

            override fun onScanFailed(errorCode: Int) {
                Log.e("ScanCallback", "Scan failed with error: $errorCode")
            }
        }

        bluetoothLeScanner?.startScan(scanCallback)
    }

    private fun connectToDevice(device: BluetoothDevice) {
        bluetoothGatt = device.connectGatt(this, false, gattCallback)
        Log.i("ScanCallback", "Connecting to ${device.name}")
    }

    private fun requestBluetoothPermissions() {
        val requestMultiplePermissions = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            permissions.entries.forEach {
                Log.d("ScanCallback", "${it.key} = ${it.value}")
            }
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestMultiplePermissions.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        }
    }
}
