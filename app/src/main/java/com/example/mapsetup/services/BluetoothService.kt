package com.example.mapsetup.services


import android.bluetooth.*
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.*
import android.location.LocationManager
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import com.example.mapsetup.models.BluetoothResponse
import com.example.mapsetup.other.BluetoothUtils
import java.util.UUID

class BluetoothService : LifecycleService() {

    private var bluetoothGatt: BluetoothGatt? = null
    private val handler = Handler(Looper.getMainLooper())
    private val retryDelay: Long = 1000 // Retry delay of 1 second

    companion object {
        var bluetoothData = MutableLiveData<BluetoothResponse>()
    }

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private val bluetoothLeScanner: BluetoothLeScanner? by lazy {
        bluetoothAdapter?.bluetoothLeScanner
    }

    // Declare the scanCallback at the class level
    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            result?.let {
                val device = it.device
                if (device.name == "MonashMap") {
                    bluetoothLeScanner?.stopScan(this) // Use 'this' to explicitly stop this callback
                    bluetoothGatt = device.connectGatt(this@BluetoothService, false, gattCallback)
                    Log.i("BluetoothService", "Connecting to ${device.name}")
                }
            }
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i("BluetoothService", "Connected to GATT server.")
                // Request a higher MTU (e.g., 512 bytes)
                gatt?.requestMtu(512)
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i("BluetoothService", "Disconnected from GATT server.")
                handler.postDelayed(reconnectRunnable, retryDelay) // Retry connection
            }
        }

        override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i("BluetoothService", "MTU changed successfully to $mtu bytes")
                gatt?.discoverServices() // Proceed to discover services after MTU change
            } else {
                Log.e("BluetoothService", "Failed to change MTU")
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val service = gatt?.getService(UUID.fromString("00000000-cc7a-482a-984a-7f2ed5b3e58f"))
                val characteristic = service?.getCharacteristic(UUID.fromString("00000001-8e22-4541-9d4c-21edae82ed19"))
                if (characteristic != null) {
                    gatt.setCharacteristicNotification(characteristic, true)
                    val descriptor = characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
                    descriptor?.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    gatt.writeDescriptor(descriptor)
                }
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
            characteristic?.value?.let {
                bluetoothData.postValue(BluetoothResponse(BluetoothUtils.bytesToInt16Array(it)))
                Log.i("BluetoothService", "Received data: ${bluetoothData.value}")
            }
        }
    }

    private val reconnectRunnable = object : Runnable {
        override fun run() {
            bluetoothGatt?.let {
                Log.i("BluetoothService", "Attempting to reconnect...")
                it.connect()
            }
        }
    }

    private val bluetoothReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                    if (state == BluetoothAdapter.STATE_ON) {
                        Log.i("BluetoothService", "Bluetooth turned ON, restarting scan...")
                        startScanning()
                    } else if (state == BluetoothAdapter.STATE_OFF) {
                        Log.i("BluetoothService", "Bluetooth turned OFF, stopping scan and disconnecting...")
                        bluetoothLeScanner?.stopScan(scanCallback)
                        bluetoothGatt?.close()
                    }
                }
                LocationManager.MODE_CHANGED_ACTION -> {
                    val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
                    if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        Log.i("BluetoothService", "Location enabled, restarting scan...")
                        startScanning()
                    } else {
                        Log.i("BluetoothService", "Location disabled, stopping scan...")
                        bluetoothLeScanner?.stopScan(scanCallback)
                    }
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        // Register Bluetooth and Location state change receivers
        val filter = IntentFilter().apply {
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
            addAction(LocationManager.MODE_CHANGED_ACTION)
        }
        registerReceiver(bluetoothReceiver, filter)
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        startScanning()
        return START_STICKY // Keep the service running
    }

    fun startScanning() {
        bluetoothLeScanner?.startScan(scanCallback) // Start scan with the scanCallback
    }

    // Optionally stop scanning and disconnect when the service is destroyed
    override fun onDestroy() {
        unregisterReceiver(bluetoothReceiver) // Unregister receiver on service destroy
        bluetoothLeScanner?.stopScan(scanCallback) // Explicitly stop the same scanCallback
        bluetoothGatt?.close()
        bluetoothGatt = null
        super.onDestroy()
    }
}
