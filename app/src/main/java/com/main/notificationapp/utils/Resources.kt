package com.main.notificationapp.utils


sealed class Resources<T> {
    data class Success<T>(val data: T) : Resources<T>()
    class Loading<T>() : Resources<T>()
    data class Error<T>(val message: String? = null) : Resources<T>()
    class InitialState<T>() : Resources<T>()
}

sealed class NewsCacheOperations {
    data class Success(val message: String) : NewsCacheOperations()
    object Loading : NewsCacheOperations()
    data class Error(val code: String? = null, val message: String? = null) : NewsCacheOperations()
}

sealed class SharedOperations {
    data class NoInternetConnection(val message: String) : SharedOperations()
}

sealed class DatastoreOperations {
    data class Updated(val isFirstLogin: Boolean, val country: String) : DatastoreOperations()
}