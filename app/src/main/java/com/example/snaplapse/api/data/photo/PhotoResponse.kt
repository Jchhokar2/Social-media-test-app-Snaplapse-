package com.example.snaplapse.api.data.photo

data class PhotoResponse(
    val id: Int,
    val user: Int,
    val location: Int,
    val description: String,
    val flags: Array<Int>,
    val likes: Array<Int>,
    val visible: Boolean,
    val bitmap: String,
    val created: String,
)
