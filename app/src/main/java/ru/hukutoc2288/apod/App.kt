package ru.hukutoc2288.apod

import android.app.Application
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

lateinit var retrofit: Retrofit
lateinit var apodApi: ApodApi

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        retrofit = retrofit2.Retrofit.Builder().baseUrl("https://api.nasa.gov/").addConverterFactory(GsonConverterFactory.create()).build()
        apodApi = retrofit.create(ApodApi::class.java)
    }
}