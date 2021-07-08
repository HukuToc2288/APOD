package ru.hukutoc2288.apod

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApodApi {
    @GET("/planetary/apod")
    fun getToday(@Query("api_key") apiKey: String=API_KEY): Call<ApodEntry>

    @GET("/planetary/apod")
    fun getRandom(@Query("count") count: Int=1, @Query("api_key") apiKey: String=API_KEY): Call<List<ApodEntry>>
}