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
import android.graphics.Color
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.charts.ScatterChart
import com.github.mikephil.charting.data.ScatterData
import com.github.mikephil.charting.data.ScatterDataSet


class Bluetooth : AppCompatActivity() {
    private lateinit var fileManager: FileManager
    var dataList: MutableList<List<Pair<Int, Int>>> = mutableListOf()
    lateinit var userInput: String
    var userLabel: String="1"
    lateinit var dataView: TextView
    lateinit var lineChart: LineChart
    private lateinit var scatterChart: ScatterChart
    private val realValuesMatrix = mutableListOf<List<Int>>()


    var countBytes = 1000001
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
                Log.i(
                    "ScanCallback",
                    "Attempting to start service discovery: ${gatt?.discoverServices()}"
                )
            } else {
                Log.e("ScanCallback", "Failed to change MTU size")
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                ggatt = gatt
                val service =
                    gatt?.getService(UUID.fromString("00000000-cc7a-482a-984a-7f2ed5b3e58f"))
                val characteristic =
                    service?.getCharacteristic(UUID.fromString("00000001-8e22-4541-9d4c-21edae82ed19"))
                characteristic2 =
                    service?.getCharacteristic(UUID.fromString("00000000-8e22-4541-9d4c-21edae82ed19"))

                if (characteristic != null) {
                    val notificationEnabled =
                        gatt.setCharacteristicNotification(characteristic, true)
                    if (notificationEnabled) {
                        Log.i("ScanCallback", "Notifications enabled for ${characteristic.uuid}")
                        val descriptor =
                            characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
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

        var timeCounter=0
        var startTime = System.currentTimeMillis()
        var FPS=0
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?
        ) {
            characteristic?.value?.let {

                val int16Values = bytesToInt16Array(it)
                writeToFile(int16Values)
                val realValues = int16Values.map { it.first }
//                Log.i("ScanCallback", realValues.toString()))
                if (timeCounter == 100) {
                    val endTime=System.currentTimeMillis()
                    FPS = (100000 / (endTime - startTime)).toInt()
                    timeCounter = 0
                    startTime=endTime
                } else {
                    timeCounter++
                }
                runOnUiThread {
                       dataView.text = "FPS :"+FPS.toString()
                        updateChart(int16Values)
                }
                updateHeatmap(int16Values)




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
        val editLabel=findViewById<TextView>(R.id.edit_label)
        val buttonSubmit = findViewById<Button>(R.id.buttonSubmit)
        dataView = findViewById(R.id.view_data)

        // Set up the Button click listener
        buttonSubmit.setOnClickListener {
            // Get the text input from the EditText
            userInput = editText.text.toString()
            userLabel = editLabel.text.toString()
            countBytes = 0
        }
        lineChart = findViewById(R.id.chart_data)
        scatterChart = findViewById(R.id.heatmap_chart)

        //line chart
    }

    @SuppressLint("MissingPermission")
    private fun startScanning() {
        if (bluetoothAdapter?.isEnabled == false) {
            Log.e("ScanCallback", "Bluetooth is off when starting scan")
            return
        } else {
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
        countBytes += 1

        if (countBytes == 101) {

            Log.i("ScanCallback", dataList.size.toString())
//            runOnUiThread {
//                Toast.makeText(this, dataList.size.toString(), Toast.LENGTH_SHORT).show()
//            }
            if (fileManager.savePairsToCsvFile(userInput + ".csv", dataList,userLabel)) {
                runOnUiThread {
                    Toast.makeText(this, "Data saved to file successfully!", Toast.LENGTH_SHORT)
                        .show()
                }
            } else {
                runOnUiThread {
                    Toast.makeText(this, "Error saving file", Toast.LENGTH_SHORT).show()
                }
            }
            dataList.clear()

        } else if (countBytes < 101) {
            dataList.add(sweep)
        }


    }

    private fun updateChart(int16Values: List<Pair<Int, Int>>) {
        val entries = ArrayList<Entry>()
        for (i in int16Values.indices) {
            val realValue = int16Values[i].first
            val imagValue = int16Values[i].second
            val RMS =
                Math.sqrt(Math.pow(realValue.toDouble(), 2.0) + Math.pow(imagValue.toDouble(), 2.0))

            // Add real and imaginary parts as separate series
            entries.add(Entry(i.toFloat(), RMS.toFloat()))  // Plot real part
        }

        val dataSet = LineDataSet(entries, "Magnitude")  // Creating dataset for real part
        val lineData = LineData(dataSet)

        // Customize Y-axis
        val leftAxis = lineChart.axisLeft
        leftAxis.setLabelCount(5, true)  // Set the number of Y-axis labels
//        leftAxis.valueFormatter = object : ValueFormatter() {
//            override fun getFormattedValue(value: Float): String {
//                return when {
//                    value >= 0 -> "$value units"  // Add "units" label for positive values
//                    else -> "$value units"  // Adjust format for negative values
//                }
//            }
//        }
        lineChart.axisLeft.isEnabled = false

        lineChart.data = lineData
        lineChart.invalidate()  // Refresh chart
    }

    private fun updateHeatmap(newList: List<Pair<Int, Int>>) {
        val realValues = newList.map {
            Math.sqrt(Math.pow(it.first.toDouble(), 2.0) + Math.pow(it.second.toDouble(), 2.0))
                .toInt()
        }
        realValuesMatrix.add(realValues)

        if (realValuesMatrix.size > 60) {
            realValuesMatrix.removeAt(0) // Limit X-axis to 30 columns
        }

        // Log matrix content for debugging

        val scatterData = generateScatterData(realValuesMatrix)
        runOnUiThread {
            scatterChart.data = scatterData
            scatterChart.notifyDataSetChanged() // Notify chart of data change
            scatterChart.invalidate() // Redraw chart
        }
    }

    private fun generateScatterData(matrix: List<List<Int>>): ScatterData {
        val entries = ArrayList<Entry>()
        val colors = ArrayList<Int>()

        for (x in matrix.indices) {
            for (y in matrix[x].indices) {
                val realValue = matrix[x][y]
                entries.add(Entry(x.toFloat(), y.toFloat()))
                entries.add(Entry(x.toFloat(), y.toFloat())) // Ensure proper Y-axis mapping
                colors.add(generateColorForValue(realValue)) // Set color based on intensity
            }
        }

        val dataSet = ScatterDataSet(entries, "Heatmap")
        dataSet.setScatterShape(ScatterChart.ScatterShape.SQUARE) // Use circle shape to avoid overlap
//        dataSet.setScatterShapeSize(5f) // Reduce size to avoid overlap
        dataSet.colors = colors

        return ScatterData(dataSet)
    }

    private fun generateColorForValue(value: Int): Int {
        // Adjust the value to a range from 0 to 255, where 0 is black and 255 is white
        val adjustedValue = (255 * Math.log(value.toDouble()) / Math.log(2500.0)).toInt()

        // Clamp the value between 0 and 255
        val grayscale = if (adjustedValue > 255) 255 else if (adjustedValue < 0) 0 else adjustedValue

        // Return a grayscale color (R = G = B = grayscale)
        return Color.rgb(grayscale, grayscale, grayscale)
    }

}







