package ru.hukutoc2288.apod.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

const val MEDIA_TYPE_VIDEO = "video"

interface ApodApi {

    @GET("/planetary/apod")
    fun getToday(@Query("api_key") apiKey: String=API_KEY, @Query("date") date: String?=null): Call<ApodEntry>

    @GET("/planetary/apod")
    fun getRandom(@Query("count") count: Int=1, @Query("api_key") apiKey: String= API_KEY): Call<List<ApodEntry>>
}