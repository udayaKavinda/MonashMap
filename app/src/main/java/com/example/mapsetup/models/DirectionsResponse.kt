package com.example.mapsetup.models

//data class DirectionsResponse(
//    val routes: List<Route>
//)
//
//data class Route(
//    val legs: List<Leg>
//)
//
//data class Leg(
//    val steps: List<Step>
//)
//
//data class Step(
//    val polyline: Polyline
//)
//
//data class Polyline(
//    val points: String
//)

data class DirectionsResponse(
    val routes: List<Route>,
    val status: String
)

data class Route(
    val overview_polyline: Polyline,
    val summary: String
)

data class Polyline(
    val points: String
)