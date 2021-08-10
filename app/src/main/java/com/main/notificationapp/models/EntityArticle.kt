package com.main.notificationapp.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class EntityArticle(
    @PrimaryKey(autoGenerate = true) var id: Int? = null,
    val author: String,
    val content: String,
    val description: String,
    val publishedAt: String,
    val source: Source,
    val title: String,
    val url: String,
    val urlToImage: String
)

object RemoteToCacheMappers {
    fun remoteToCacheConverter(article: Article) : EntityArticle {
        return EntityArticle(
            null,
            article.author,
            article.content,
            article.description,
            article.publishedAt,
            article.source,
            article.title,
            article.url,
            article.urlToImage
        )
    }

    fun cacheToRemoteConverter(entityArticle: EntityArticle) : Article {
        return Article(
            entityArticle.author,
            entityArticle.content,
            entityArticle.description,
            entityArticle.publishedAt,
            entityArticle.source,
            entityArticle.title,
            entityArticle.url,
            entityArticle.urlToImage
        )
    }
}