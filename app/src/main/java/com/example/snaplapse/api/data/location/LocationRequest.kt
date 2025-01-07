package com.example.snaplapse.api.data.location

data class LocationRequest(
    val name: String,
    val longitude: Double,
    val latitude: Double,
    val categories: List<Int>,
    val google_id: String
)
