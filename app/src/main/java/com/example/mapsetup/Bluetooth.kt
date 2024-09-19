package com.example.mapsetup

import FileManager
import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import org.json.JSONObject
import java.util.UUID
import android.content.ContentValues
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

class Bluetooth : AppCompatActivity() {
    private lateinit var fileManager: FileManager
    var dataList: MutableList<List<Pair<Int, Int>>> = mutableListOf()
    lateinit var userInput :String
    lateinit var dataView: TextView

    var countBytes=1000001
    var characteristic2: BluetoothGattCharacteristic? = null
    var ggatt: BluetoothGatt? = null
    var value = 0

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private val bluetoothLeScanner by lazy {
        bluetoothAdapter?.bluetoothLeScanner
    }

    private var bluetoothGatt: BluetoothGatt? = null
    private val handler = Handler(Looper.getMainLooper())
    private val retryDelay: Long = 1000 // 1 second retry delay

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i("ScanCallback", "Connected to GATT server.")
                gatt?.requestMtu(512)
                handler.removeCallbacks(reconnectRunnable) // Stop retries if connected
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i("ScanCallback", "Disconnected from GATT server.")
                handler.postDelayed(reconnectRunnable, retryDelay) // Schedule reconnection
            }
        }

        override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
            super.onMtuChanged(gatt, mtu, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i("ScanCallback", "MTU size changed to $mtu")
                Log.i("ScanCallback", "Attempting to start service discovery: ${gatt?.discoverServices()}")
            } else {
                Log.e("ScanCallback", "Failed to change MTU size")
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                ggatt = gatt
                val service = gatt?.getService(UUID.fromString("00000000-cc7a-482a-984a-7f2ed5b3e58f"))
                val characteristic = service?.getCharacteristic(UUID.fromString("00000001-8e22-4541-9d4c-21edae82ed19"))
                characteristic2 = service?.getCharacteristic(UUID.fromString("00000000-8e22-4541-9d4c-21edae82ed19"))

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

        override fun onCharacteristicRead(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                characteristic?.value?.let {
                    Log.i("ScanCallback", "Characteristic read: ${it.joinToString()}")
                }
            }
        }

        override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i("ScanCallback", "Characteristic written successfully")
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
            characteristic?.value?.let {


                val int16Values = bytesToInt16Array(it)
                writeToFile(int16Values)
                runOnUiThread {
                    if (dataView != null) {
                        dataView.text = int16Values.toString()
                    }}


//                Log.i("ScanCallback", int16Values.toString())
            }
        }
    }

    // Runnable to handle reconnection attempts
    private val reconnectRunnable = object : Runnable {
        override fun run() {
            bluetoothGatt?.let {
                Log.i("ScanCallback", "Attempting to reconnect...")
                it.connect() // Attempt reconnection
                handler.postDelayed(this, retryDelay) // Retry every second
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bluetooth)
        fileManager = FileManager(this)


        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        registerReceiver(bluetoothStateReceiver, filter)

        startScanning()

        val sendButton = findViewById<Button>(R.id.send_button)
        sendButton.setOnClickListener {
            characteristic2?.setValue(byteArrayOf(0x01))
            ggatt?.writeCharacteristic(characteristic2)
        }

        val editText = findViewById<EditText>(R.id.editText)
        val buttonSubmit = findViewById<Button>(R.id.buttonSubmit)
        dataView = findViewById(R.id.view_data)

        // Set up the Button click listener
        buttonSubmit.setOnClickListener {
            // Get the text input from the EditText
            userInput = editText.text.toString()
            countBytes=0


            // Show the input as a Toast or process it as needed
//            Toast.makeText(this, "You entered: $userInput", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("MissingPermission")
    private fun startScanning() {
        if (bluetoothAdapter?.isEnabled == false) {
            Log.e("ScanCallback", "Bluetooth is off when starting scan")
            return
        }else{
            Log.i("ScanCallback", "starting scan")
        }
        val scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                super.onScanResult(callbackType, result)
                result?.let {
                    val device = it.device
                    Log.i("ScanCallback", "Device found: ${device.name} - ${device.address}")
                    if (device.name == "MonashMap") {
                        // Stop scanning and connect to the device
                        bluetoothLeScanner?.stopScan(this)
                        bluetoothGatt = device.connectGatt(baseContext, false, gattCallback)
                        Log.i("ScanCallback", "Connecting to ${device.name}")
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

    fun bytesToInt16Array(data: ByteArray): List<Pair<Int, Int>> {
        val int16Values = mutableListOf<Pair<Int, Int>>()
        for (i in 0 until data.size - 3 step 4) {
            val higherByteReal = data[i].toInt()
            val lowerByteReal = data[i + 1].toInt()

            val higherByteImag = data[i + 2].toInt()
            val lowerByteImag = data[i + 3].toInt()

            var valueReal = (higherByteReal shl 8) or (lowerByteReal and 0xFF)
            var valueImag = (higherByteImag shl 8) or (lowerByteImag and 0xFF)

            if (valueReal > 0x7FFF) valueReal -= 0x10000
            if (valueImag > 0x7FFF) valueImag -= 0x10000

            int16Values.add(Pair(valueReal, valueImag))
        }
        return int16Values
    }

    private val bluetoothStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (BluetoothAdapter.ACTION_STATE_CHANGED == intent?.action) {
                val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                when (state) {
                    BluetoothAdapter.STATE_OFF -> {
                        Log.i("ScanCallback", "Bluetooth is OFF")
                    }
                    BluetoothAdapter.STATE_ON -> {
                        Log.i("ScanCallback", "Bluetooth is ON")
                        startScanning()
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(bluetoothStateReceiver)
        handler.removeCallbacks(reconnectRunnable)
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
    }

    fun writeToFile(sweep: List<Pair<Int, Int>>) {
        countBytes+=1

        if (countBytes==101){

            Log.i("ScanCallback", dataList.size.toString())
            runOnUiThread {
                Toast.makeText(this, dataList.size.toString(), Toast.LENGTH_SHORT).show()
            }
            if(fileManager.savePairsToCsvFile( userInput+".csv", dataList)){
                runOnUiThread {
                    Toast.makeText(this, "Data saved to file successfully!", Toast.LENGTH_SHORT).show()
                }
            }else{
                runOnUiThread {
                    Toast.makeText(this, "Error saving file", Toast.LENGTH_SHORT).show()
                }
            }
            dataList.clear()

        }else if(countBytes<101){
            dataList.add(sweep)
        }



    }

}



























