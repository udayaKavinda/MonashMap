package com.example.mapsetup

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.mapsetup.api.PredictionServiceApi
import com.example.mapsetup.api.WeatherServiceApi
import com.example.mapsetup.managers.ChartManager
import com.example.mapsetup.managers.FileManager
import com.example.mapsetup.models.BluetoothResponse
import com.example.mapsetup.models.PredictRequest
import com.example.mapsetup.models.PredictResponse
import com.example.mapsetup.other.RadarDataProcessor
import com.example.mapsetup.services.BluetoothService
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.ScatterChart
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class Bluetooth : AppCompatActivity() {
    private lateinit var retrofit: Retrofit
    private lateinit var apiService: PredictionServiceApi
    private var prediction: String = "Pending"

    private lateinit var chartManager: ChartManager
    private lateinit var dataView: TextView
    private lateinit var fileManager: FileManager  // Create FileManager instance
    private var dataList: MutableList<BluetoothResponse> = mutableListOf()

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



        retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.1.53:5000/")  // Replace with your actual server URL
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(PredictionServiceApi::class.java)

    }

    private fun subscribeToObservers() {
        BluetoothService.bluetoothData.observe(this, Observer { bluetoothData ->
            bluetoothData?.let {
                RadarDataProcessor.collect100Sweeps(it.radar_data, fileManager) // Pass FileManager to DataProcessor
                dataList.add(it)
                if(dataList.size==100) {
                    requestPrediction()

                }else if(dataList.size>100){
                    dataList.clear()
                }
                runOnUiThread {
                    chartManager.updateLineChart(it.radar_data) // Update Line Chart via ChartManager
                    chartManager.updateHeatmap(it.radar_data) // Update Heatmap via ChartManager
                    dataView.text = "Prediction: $prediction"
                }
            }
        })
    }
    private fun requestPrediction() {
        val request = PredictRequest(data = dataList)
        Log.i("Bluetoothz", "Request")
        val call = apiService.getPrediction(request)
        call.enqueue(object : retrofit2.Callback<PredictResponse> {
            override fun onResponse(call: Call<PredictResponse>, response: retrofit2.Response<PredictResponse>) {
                if (response.isSuccessful) {
                    val intPrediction = response.body()?.prediction
                    if(intPrediction==0){
                        prediction="desk"
                    }
                    else if(intPrediction==1){
                        prediction="carpet"
                    }
                    else if(intPrediction==2){
                        prediction="floor"
                    }
                    else if(intPrediction==3){
                        prediction="concrete"
                    }
                    else if(intPrediction==4){
                        prediction="sealed concrete"
                    }
                    else{
                        prediction="other"
                    }
                    Log.i("Bluetoothz", "Prediction: $prediction")
                    // Use the prediction result, for example, display it in the UI
                } else {
                    Log.i("Bluetoothz", "Failed to get a valid response")
                    // Handle the error
                }
            }

            override fun onFailure(call: Call<PredictResponse>, t: Throwable) {
                // Handle failure (e.g., network issues)
                t.printStackTrace()
                Log.i("Bluetoothz", t.toString())
                // Handle the error
            }
        })
    }

}
