package com.example.snaplapse.view_models

import com.google.android.libraries.places.api.model.Place

data class CurrentPlaceViewModel(
    val name: String,
    val address: String,
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val types: List<String>
)
