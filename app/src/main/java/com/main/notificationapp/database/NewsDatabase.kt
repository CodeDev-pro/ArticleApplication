package com.main.notificationapp.database

import android.content.Context
import androidx.room.*
import com.main.notificationapp.models.Article
import com.main.notificationapp.models.EntityArticle

@Database(entities = [EntityArticle::class], version = 1, exportSchema = false)
@TypeConverters(Converter::class)
abstract class NewsDatabase : RoomDatabase() {
    abstract fun getNewsDao() : NewsDao

    companion object{
        private var instance:NewsDatabase? = null

        operator fun invoke(context: Context) = instance?: synchronized(this){
            instance?:createDatabase(context).also{
                instance = it
            }
        }

        private fun createDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                NewsDatabase::class.java,
                "articles_db.db"
            ).build()
    }
}