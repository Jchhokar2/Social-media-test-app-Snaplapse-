package com.example.snaplapse.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

object MapHelper {

    private const val baseUrl="https://maps.googleapis.com/"

    fun getInstance(): Retrofit {

        var mHttpLoggingInterceptor = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)

        var mOkHttpClient = OkHttpClient.Builder().addInterceptor(mHttpLoggingInterceptor).build()

        return Retrofit.Builder().baseUrl(MapHelper.baseUrl)
            .addConverterFactory(ScalarsConverterFactory.create())
            .client(mOkHttpClient)
            // we need to add converter factory to
            // convert JSON object to Java object
            .build()
    }
}