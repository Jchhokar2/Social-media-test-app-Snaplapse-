package com.example.snaplapse.api.data.photo

data class PhotoListResponse(
    val count: Int,
    val results: List<PhotoResponse>,
)
