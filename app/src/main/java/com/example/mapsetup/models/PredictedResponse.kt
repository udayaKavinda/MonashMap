package com.example.mapsetup.models

data class PredictRequest(
    val data: List<BluetoothResponse>  // List of features as per your format
)

// Model for response
data class PredictResponse(
    val prediction: Int  // Prediction result from server
)