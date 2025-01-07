package com.example.snaplapse.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitHelper {

    private const val baseUrl = "https://snaplapse.herokuapp.com/"
//    private const val baseUrl="http://10.0.2.2:8000/"

    fun getInstance(): Retrofit {

        var mHttpLoggingInterceptor = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)

        var mOkHttpClient = OkHttpClient.Builder().addInterceptor(mHttpLoggingInterceptor).build()

        return Retrofit.Builder().baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .client(mOkHttpClient)
            // we need to add converter factory to
            // convert JSON object to Java object
            .build()
    }
}
