package com.example.snaplapse.api.data.location

data class LocationListResponse(
    val count: Int,
    val next: Any,
    val previous: Any,
    val results: List<LocationResponse>
)
