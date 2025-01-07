package com.example.snaplapse.api.data.user

data class UserList(
    val count: Int,
    val next: String,
    val previous: String,
    val results: List<User>
)
