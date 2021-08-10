package com.main.notificationapp.repositories

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

class DatastoreRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val Context.datastore by preferencesDataStore(name = "user_datastore")
    val userPreferences = context.datastore.data
        .catch { exception ->
            if(exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map {
            val country = it[PreferencesKeys.COUNTRY] ?: "us"
            //val isFirstLogin = it[PreferencesKeys.IS_FIRST_LOGIN] ?: true

            country
        }

    suspend fun saveUserState(country: String) {
        context.datastore.edit {
            it[PreferencesKeys.COUNTRY] = country
            //it[PreferencesKeys.IS_FIRST_LOGIN] = isFirstLogin ?: true
        }
    }

    private object PreferencesKeys {
        val COUNTRY = stringPreferencesKey("country")
        //val IS_FIRST_LOGIN = booleanPreferencesKey("is_first_login")
    }
}