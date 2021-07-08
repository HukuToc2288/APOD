package ru.hukutoc2288.apod

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApodApi {
    @GET("/planetary/apod")
    fun getData(@Query("api_key") apiKey: String): Call<List<ApodEntry>>
}