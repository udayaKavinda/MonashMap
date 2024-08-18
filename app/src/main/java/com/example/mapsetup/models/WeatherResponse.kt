package com.example.mapsetup.models

data class WeatherResponse(
    val latitude: Double,
    val longitude: Double,
    val generationtime_ms: Double,
    val utc_offset_seconds: Int,
    val timezone: String,
    val timezone_abbreviation: String,
    val elevation: Int,
    val current_units: CurrentUnits,
    val current: Current
)

data class CurrentUnits(
    val time: String,
    val interval: String,
    val temperature_2m: String,
    val relative_humidity_2m: String,
    val apparent_temperature: String,
    val precipitation: String,
    val cloud_cover: String,
    val surface_pressure: String,
    val wind_speed_10m: String,
    val wind_direction_10m: String
)

data class Current(
    val time: String,
    val interval: Int,
    val temperature_2m: Double,
    val relative_humidity_2m: Int,
    val apparent_temperature: Double,
    val precipitation: Double,
    val cloud_cover: Int,
    val surface_pressure: Double,
    val wind_speed_10m: Double,
    val wind_direction_10m: Int
)
