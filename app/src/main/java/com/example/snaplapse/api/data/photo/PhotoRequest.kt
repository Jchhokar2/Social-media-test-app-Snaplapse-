package com.example.snaplapse.api.data.photo

import android.graphics.Bitmap

data class PhotoRequest(
    val user: Int,
    val location: Int,
    val description: String,
    val bitmap: String,
)
