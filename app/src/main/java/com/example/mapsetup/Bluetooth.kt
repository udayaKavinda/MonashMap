package com.example.mapsetup

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.mapsetup.managers.ChartManager
import com.example.mapsetup.managers.FileManager
import com.example.mapsetup.other.RadarDataProcessor
import com.example.mapsetup.services.BluetoothService
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.ScatterChart

class Bluetooth : AppCompatActivity() {

    private lateinit var chartManager: ChartManager
    private lateinit var dataView: TextView
    private lateinit var fileManager: FileManager  // Create FileManager instance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bluetooth)

        fileManager = FileManager(this)  // Initialize FileManager with context

        val lineChart = findViewById<LineChart>(R.id.chart_data)
        val scatterChart = findViewById<ScatterChart>(R.id.heatmap_chart)
        val dataCollectButton = findViewById<Button>(R.id.buttonSubmit)
        val fileNameEditText = findViewById<EditText>(R.id.file_name)
        val categoryLabelEditText = findViewById<EditText>(R.id.category_label)

        dataCollectButton.setOnClickListener {
            val fileName = fileNameEditText.text.toString()
            val categoryLabel = categoryLabelEditText.text.toString()
//            Log.i("Bluetoothi", "File Name: $fileName, Category Label: $categoryLabel")
            RadarDataProcessor.saveDataToFile(fileName,categoryLabel)
        }

        dataView = findViewById(R.id.view_data)
        chartManager = ChartManager(lineChart, scatterChart, this)
        subscribeToObservers()
    }

    private fun subscribeToObservers() {
        BluetoothService.bluetoothData.observe(this, Observer { bluetoothData ->
            bluetoothData?.let {
                RadarDataProcessor.collect100Sweeps(it.radar_data, fileManager) // Pass FileManager to DataProcessor
                runOnUiThread {
                    chartManager.updateLineChart(it.radar_data) // Update Line Chart via ChartManager
                    chartManager.updateHeatmap(it.radar_data) // Update Heatmap via ChartManager
                }
            }
        })
    }

}
