package com.example.snaplapse.view_models

import android.graphics.Bitmap

data class ItemsViewModel2(
    val id: Int,
    val user: Int,
    val image: Bitmap,
    val text: String,
    val date: String,
    val visible: Boolean
    )
