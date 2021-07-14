package ru.hukutoc2288.apod

import android.app.Application
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import org.conscrypt.Conscrypt
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.Security

lateinit var retrofit: Retrofit
lateinit var apodApi: ApodApi

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        Security.insertProviderAt(Conscrypt.newProvider(), 1);
        retrofit = Retrofit.Builder().baseUrl("https://api.nasa.gov/").addConverterFactory(GsonConverterFactory.create()).build()
        apodApi = retrofit.create(ApodApi::class.java)
    }
}