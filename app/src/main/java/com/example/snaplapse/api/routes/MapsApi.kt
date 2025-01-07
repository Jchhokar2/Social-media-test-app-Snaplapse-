package com.example.snaplapse.api.routes

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.QueryMap

interface MapsApi {
    @GET("/maps/api/place/nearbysearch/json")
    suspend fun getNearbyPlaces(@QueryMap params: Map<String, String>): Response<String>

    @GET("/maps/api/place/findplacefromtext/json")
    suspend fun findPlaceFromText(@QueryMap params: Map<String, String>): Response<String>
}