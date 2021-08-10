package com.main.notificationapp.repositories

import android.util.Log
import com.main.notificationapp.api.NewsApi
import com.main.notificationapp.database.NewsDao
import com.main.notificationapp.models.Article
import com.main.notificationapp.models.EntityArticle
import com.main.notificationapp.models.NewsResponse
import com.main.notificationapp.models.SourcesResponse
import com.main.notificationapp.utils.Resources
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject


private const val TAG = "MAIN_REPOSITORY"

class MainRepository @Inject constructor(private val newsDao: NewsDao, private val newsApi: NewsApi) {

    suspend fun getSources() : Flow<Resources<SourcesResponse>> = flow {
        emit(Resources.Loading())
        try {
            val response = newsApi.getSources()
            if(response.isSuccessful) {
                response.body()?.let {
                    Log.d(TAG, "getSources: ${it.sources.toString()}")
                    emit(Resources.Success<SourcesResponse>(it))
                }
            } else {
                Log.d(TAG, "getSources: ${response.toString()}")
                emit(Resources.Error<SourcesResponse>(message = response.message()))
            }
        }catch (e: Exception) {
            Log.d(TAG, "getSources: ${e.toString()}")
            emit(Resources.Error<SourcesResponse>(message = e.toString()))
        }
    }

    suspend fun searchArticle(keyword: String, page: Int) : Flow<Resources<NewsResponse>> = flow {
        emit(Resources.Loading<NewsResponse>())
        try {
            val response = newsApi.searchArticle(query = keyword, page = page.toString())
            if(response.isSuccessful){
                response.body()?.let {
                    emit(Resources.Success<NewsResponse>(data = it))
                    Log.d(TAG, "searchArticle: ${it.toString()}")
                }
            }else {
                emit(Resources.Error<NewsResponse>(message = response.message().toString()))
                Log.d(TAG, "searchArticle: ${response.toString()}")
            }
        }catch (e: Exception){
            emit(Resources.Error<NewsResponse>(message = e.toString()))
            Log.d(TAG, "searchArticle: $e")
        }
    }

    suspend fun getEverythingFromNewsSource(page: String): Flow<Resources<NewsResponse>> = flow {
        emit(Resources.Loading<NewsResponse>())
        try {
            val response = newsApi.getEverything()
            if(response.isSuccessful) {
                response.body()?.let {
                    emit(Resources.Success<NewsResponse>(it))
                }
            } else emit(Resources.Error<NewsResponse>(message = response.message()))
        }catch (e: Exception) {
            emit(Resources.Error<NewsResponse>(message = e.toString()))
        }
    }

    suspend fun saveArticle(article: EntityArticle) = newsDao.insertOrUpdate(article)

    suspend fun getAllCacheArticles() : Flow<Resources<List<EntityArticle>>> = flow {
        emit(Resources.Loading<List<EntityArticle>>())
        try {
            val articles = newsDao.getAllArticles().collect {
                emit(Resources.Success<List<EntityArticle>>(it))
                Log.d(TAG, "getAllCacheArticles: ${it.toString()}")
            }
        }catch (e: Exception) {
            Log.d(TAG,"getAllCacheArticles: $e")
            emit(Resources.Error<List<EntityArticle>>(message = e.toString()))
        }
    }

    suspend fun deleteCacheArticle(article: EntityArticle) =
        newsDao.deleteArticle(article)

    fun getCacheArticleContent(url: String) = newsDao.getArticleContent(url)

    suspend fun getTopHeadlines(country: String, page: Int) : Flow<Resources<NewsResponse>> = flow {
        Timber.d("getTopHeadlines")
        emit(Resources.Loading<NewsResponse>())
        try{
            val response = newsApi.getTopHeadlines(page = page.toString(), country = country)
            if(response.isSuccessful){
                response.body()?.let {
                    Log.d(TAG, "getTopHeadlines $response")
                    emit(Resources.Success<NewsResponse>(data = it))
                }
            }else {
                Log.d(TAG, "getTopHeadlines $response")
                emit(Resources.Error<NewsResponse>(response.toString()))
            }
        }catch (e: Exception){
            Log.d(TAG, "getTopHeadlines $e")
            emit(Resources.Error<NewsResponse>(message = e.toString()))
        }
    }
}