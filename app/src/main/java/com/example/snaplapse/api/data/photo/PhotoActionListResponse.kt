package com.example.snaplapse.api.data.photo

data class PhotoActionListResponse(
    val count: Int,
    val next: Any,
    val previous: Any,
    val results: List<PhotoActionResponse>
)
