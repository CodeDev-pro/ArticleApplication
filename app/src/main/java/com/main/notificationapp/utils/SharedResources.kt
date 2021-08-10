package com.main.notificationapp.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewTreeLifecycleOwner
import com.main.notificationapp.BaseApplication
import com.main.notificationapp.models.NewsResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import timber.log.Timber
import kotlin.random.Random

const val TAG = "articles"

object SharedResources {

    private suspend fun <T> executeFlowData(
        response: Resources<T>,
        success: suspend (Resources.Success<T>) -> (Unit),
        error: (Resources.Error<T>) -> (Unit),
        loading: (Resources.Loading<T>) -> (Unit),
        initialState: (Resources.InitialState<T>) -> (Unit)
    ) {
        when(response) {
            is Resources.Loading -> {
                Log.d(TAG, "executeFlowData ${response.toString()}")
                loading(Resources.Loading())
            }
            is Resources.Success -> {
                Log.d(TAG, "executeFlowData: ${response.toString()} success")
                success(Resources.Success(response.data))
            }
            is Resources.Error -> {
                Log.d(TAG, "executeFlowData error ${response.toString()}")
                error(Resources.Error(response.message))
            }
            is Resources.InitialState -> {
                Log.d(TAG, "executeFlowData initialstate ${response.toString()}")
                initialState(Resources.InitialState())
            }
        }
    }

    fun <T> LiveData<Resources<T>>.observeAndExecute(
        lifecycleOwner: LifecycleOwner,
        error: (Resources.Error<T>) -> Unit,
        loading: () -> Unit,
        success: (Resources.Success<T>) -> Unit,
        initialState: () -> Unit
    ) {
        this.observe(
            lifecycleOwner
        ){ resources ->
            when(resources) {
                is Resources.Loading -> {

                    loading()
                }
                is Resources.Success -> {
                    success(Resources.Success(resources.data))
                }
                is Resources.Error -> {
                    error(Resources.Error(resources.message))
                }
                is Resources.InitialState -> {
                    initialState()
                }
            }
        }
    }

    suspend fun <T> Flow<Resources<T>>.collectAndExecute(_liveData: MutableLiveData<Resources<T>>, app: BaseApplication, extras: suspend (Resources.Success<T>) -> (Unit)) {
        checkInternetConnection(
            app,
            action = {
                this.collect { event ->
                    executeFlowData(
                        event,
                        success = {
                            _liveData.postValue(it)
                            extras(it)
                        },
                        error = {
                            _liveData.postValue(it)
                        },
                        loading = {
                            _liveData.postValue(it)
                        },
                        initialState = {
                            _liveData.postValue(it)
                        }
                    )
                }
            },
            error = {
                _liveData.postValue(Resources.Error(it))
            }
        )
    }

    private suspend fun checkInternetConnection(app: BaseApplication, action: suspend () -> (Unit), error: (String) -> Unit) {
        if (hasInternetConnection(app)){
            Timber.d("has internet")
            action()

        }else {
            Timber.d("no internet")
            error("No Internet Connection")
        }
    }

    fun hasInternetConnection(app: BaseApplication): Boolean {
        val connectivityManager =
            app.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val capabilities =
                connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
            return when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            connectivityManager.activeNetworkInfo?.run {
                return when (type) {
                    ConnectivityManager.TYPE_WIFI -> true
                    ConnectivityManager.TYPE_ETHERNET -> true
                    ConnectivityManager.TYPE_MOBILE -> true
                    else -> false
                }
            }
        }
        return false
    }
}