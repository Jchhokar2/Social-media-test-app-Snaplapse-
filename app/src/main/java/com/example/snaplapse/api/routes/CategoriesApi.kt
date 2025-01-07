package com.example.snaplapse.api.routes

import com.example.snaplapse.api.data.category.CategoryRequest
import com.example.snaplapse.api.data.category.CategoryResponse
import retrofit2.Response
import retrofit2.http.*

interface CategoriesApi {
    @GET("/api/categories/{name}")
    suspend fun getCategoryByName(@Path("name") name: String): Response<CategoryResponse>

    @POST("/api/categories/")
    suspend fun createCategory(@Body categoryRequest: CategoryRequest): Response<CategoryResponse>

    @PATCH("/api/categories/{id}")
    suspend fun editCategory(@Path("id") id: Int): Response<String>

    @DELETE("/api/categories/{id}")
    suspend fun delete(@Path("id") id: Int): Response<String>
}