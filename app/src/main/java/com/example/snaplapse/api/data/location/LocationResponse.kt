package com.example.snaplapse.api.data.location

data class LocationResponse(
    val id: Int,
    val name: String,
    val longitude: Double,
    val latitude: Double,
    val photos: List<Int>,
    val tags: List<Int>,
    val categories: List<Int>,
    val created: String,
    val google_id: String
)
