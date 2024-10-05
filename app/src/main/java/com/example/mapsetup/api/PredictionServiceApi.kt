package com.example.mapsetup.api

import com.example.mapsetup.models.PredictRequest
import com.example.mapsetup.models.PredictResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface PredictionServiceApi {
    @POST("/predict")
    fun getPrediction(@Body request: PredictRequest): Call<PredictResponse>
}
