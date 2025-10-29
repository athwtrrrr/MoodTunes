package com.example.myspecial.moodtunes.data.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface DeezerService {


    @GET("chart/{genreId}")
    suspend fun getGenreChart(
        @Path("genreId") genreId: Int,
        @Query("limit") limit: Int = 25
    ): DeezerChartResponse




    companion object {
        fun createApiService(): DeezerService {
            val retrofit = Retrofit.Builder()
                .baseUrl("https://api.deezer.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            return retrofit.create(DeezerService::class.java)
        }
    }
}