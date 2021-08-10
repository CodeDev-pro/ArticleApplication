package com.main.notificationapp.api

import com.main.notificationapp.models.NewsResponse
import com.main.notificationapp.models.SourcesResponse
import com.main.notificationapp.utils.Constants
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApi {

    @GET("/v2/top-headlines")
    suspend fun getTopHeadlines(
        @Query("apiKey") apiKey: String = Constants.API_KEY,
        @Query("country") country: String = "us",
        @Query("page") page: String = "1"
    ) : Response<NewsResponse>

    @GET("/v2/everything")
    suspend fun getEverything(
        @Query("q") query: String = "everything",
        @Query("page") page: String = "1",
        @Query("apiKey") apiKey: String = Constants.API_KEY
    ) : Response<NewsResponse>

    @GET("/v2/everything")
    suspend fun searchArticle(
        @Query("q") query: String,
        @Query("apiKey") apiKey: String = Constants.API_KEY,
        @Query("page") page: String = "1"
    ) : Response<NewsResponse>

    @GET("/v2/sources")
    suspend fun getSources(
        @Query("apiKey") apiKey: String = Constants.API_KEY,
        @Query("language") language: String = "en",
        @Query("country") countryName: String = "us"
    ) : Response<SourcesResponse>
}