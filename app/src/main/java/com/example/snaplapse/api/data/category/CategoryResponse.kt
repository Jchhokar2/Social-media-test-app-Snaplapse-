package com.example.snaplapse.api.data.category

data class CategoryResponse(
    val id: Int,
    val name: String,
    val tags: List<Int>,
    val created: String
)
