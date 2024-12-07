package com.ewan.triviaapp.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object TriviaApiService {
    private const val BASE_URL = "https://opentdb.com/"

    val api: TriviaApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TriviaApi::class.java)
    }
}