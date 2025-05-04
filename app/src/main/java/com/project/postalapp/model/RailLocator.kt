package com.project.postalapp.model

data class RailLocator(
    val id:Int,
    val stationName: String,
    val stationMobile: String,
    val location: String,
    val chargingpoints: String,
    val type: String,
    val role: String,
    val adapterTypes: List<String>
)
