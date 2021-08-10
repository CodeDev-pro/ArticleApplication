package com.main.notificationapp.database

import androidx.room.*
import com.main.notificationapp.models.Article
import com.main.notificationapp.models.EntityArticle
import kotlinx.coroutines.flow.Flow

@Dao
interface NewsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(article: EntityArticle)

    @Delete
    suspend fun deleteArticle(article: EntityArticle)

    @Query("SELECT * FROM entityarticle")
    fun getAllArticles() : Flow<List<EntityArticle>>

    @Query("SELECT content FROM entityarticle WHERE url = :url")
    fun getArticleContent(url: String) : String
}