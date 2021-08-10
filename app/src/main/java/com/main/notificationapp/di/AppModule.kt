package com.main.notificationapp.di

import android.content.Context
import com.main.notificationapp.BaseApplication
import com.main.notificationapp.api.NewsApi
import com.main.notificationapp.database.NewsDao
import com.main.notificationapp.database.NewsDatabase
import com.main.notificationapp.repositories.DatastoreRepository
import com.main.notificationapp.repositories.MainRepository
import com.main.notificationapp.utils.Constants.BASE_URL
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideNewsDao(@ApplicationContext context: Context) : NewsDao {
        return NewsDatabase(context).getNewsDao()
    }

    @Provides
    @Singleton
    fun provideNewsApi() : NewsApi {
        val logging = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
        val client = OkHttpClient.Builder().addInterceptor(logging).build()
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NewsApi::class.java)
    }

    @Provides
    @Singleton
    fun provideMainRepository(newsApi: NewsApi, newsDao: NewsDao) : MainRepository {
        return MainRepository(newsDao, newsApi)
    }

    @Provides
    @Singleton
    fun provideDatastoreRepository(@ApplicationContext context: Context) = DatastoreRepository(context)
}