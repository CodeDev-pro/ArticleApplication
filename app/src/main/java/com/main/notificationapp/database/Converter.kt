package com.main.notificationapp.database

import androidx.room.TypeConverter
import com.main.notificationapp.models.Source

class Converter {

    @TypeConverter
    fun fromSource(source: Source): String{
        return source.name
    }
    @TypeConverter
    fun fromName(name: String): Source{
        return Source(name, name)
    }
}