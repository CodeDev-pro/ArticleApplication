package com.main.notificationapp.ui.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.main.notificationapp.models.Article
import com.main.notificationapp.models.EntityArticle
import com.main.notificationapp.models.NewsResponse
import com.main.notificationapp.models.SourcesResponse
import com.main.notificationapp.repositories.DatastoreRepository
import com.main.notificationapp.repositories.MainRepository
import com.main.notificationapp.utils.DatastoreOperations
import com.main.notificationapp.utils.NewsCacheOperations
import com.main.notificationapp.utils.Resources
import com.main.notificationapp.utils.SharedResources
import com.main.notificationapp.utils.SharedResources.collectAndExecute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.Exception
import kotlin.random.Random

private const val TAG = "MainViewModel"

@HiltViewModel
class MainViewModel
    @Inject constructor(
        private val repository: MainRepository,
        private val datastoreRepository: DatastoreRepository,
        app: Application,
    ) : AndroidViewModel(app) {

    var savedNewsFirstLaunch = true
    var topHeadlinesFirstLaunch = true
    var sourcesFirstLaunch = true
    var searchFragmentFirstLaunch = true
    //val profileFragmentFirstLaunch = MutableLiveData(false)
    val currentCountry = MutableLiveData("Current Country: us")


    private val _cacheOperation: Channel<NewsCacheOperations> = Channel()
    val cacheOperations get() = _cacheOperation.receiveAsFlow()

    private val _cacheArticles: MutableLiveData<Resources<List<EntityArticle>>> =
        MutableLiveData(Resources.InitialState())
    val cacheArticles: LiveData<Resources<List<EntityArticle>>> get() = _cacheArticles

    private val _topHeadlinesArticles: MutableLiveData<Resources<NewsResponse>> =
        MutableLiveData()
    val topHeadlinesArticles: LiveData<Resources<NewsResponse>> get() = _topHeadlinesArticles

    private val _sources: MutableLiveData<Resources<SourcesResponse>> =
        MutableLiveData()
    val sources: LiveData<Resources<SourcesResponse>> get() = _sources

    private val _notificationArticle: Channel<Article> = Channel()
    val notificationArticle get() = _notificationArticle.receiveAsFlow()

    private val _searchedArticles: MutableLiveData<Resources<NewsResponse>> =
        MutableLiveData()
    val searchedArticles: LiveData<Resources<NewsResponse>>
        get() =
            _searchedArticles

    private val _cacheArticleContent: MutableLiveData<Resources<String>> =
        MutableLiveData()
    val cacheArticleContent: LiveData<Resources<String>> =
        _cacheArticleContent

    private val _profileEvents: Channel<NewsCacheOperations> = Channel()
    val profileEvents = _profileEvents.receiveAsFlow()

    private val _loginEvents: Channel<DatastoreOperations> = Channel()
    val loginEvents = _loginEvents.receiveAsFlow()
    var isFirstLogIn = true

    val articleProgress = MutableLiveData(0)

    init {
        checkLoginState()
        if(topHeadlinesFirstLaunch) {
            safeTopHeadlinesCall(page = 1)
            topHeadlinesFirstLaunch = false
        }
        if(savedNewsFirstLaunch) {
            getAllCacheArticles()
            savedNewsFirstLaunch = false
        }
        if(sourcesFirstLaunch) {
            getArticleSources()
            sourcesFirstLaunch = false
        }
    }

    fun initState() {
        _topHeadlinesArticles.postValue(Resources.InitialState())
        _cacheArticles.postValue(Resources.InitialState())
        _sources.postValue(Resources.InitialState())
        _searchedArticles.postValue(Resources.InitialState())
        _cacheArticleContent.postValue(Resources.InitialState())
    }


    fun updateUserInfo(country: String) = viewModelScope.launch {
        try {
            _profileEvents.send(NewsCacheOperations.Loading)
            datastoreRepository.saveUserState(country = country)
            delay(2000)
            _profileEvents.send(NewsCacheOperations.Success("Saved Successfully"))

        } catch (e: Exception) {
            _profileEvents.send(NewsCacheOperations.Error(message = e.toString()))
        }
    }

    fun checkLoginState() = viewModelScope.launch {
        datastoreRepository.userPreferences.collect { country ->
            //SharedResources.log("checkLoginState loading ${it.toString()}")
            currentCountry.postValue("Current Country: $country")
            //_loginEvents.send(DatastoreOperations.Updated(isFirstLogIn, country))
        }
    }


    fun safeSearchArticleCall(keyword: String, page: Int) = viewModelScope.launch {

        repository.searchArticle(keyword, page).collectAndExecute(
            _liveData = _searchedArticles,
            getApplication()
        ) {

        }
    }

    fun safeTopHeadlinesCall(page: Int) = viewModelScope.launch {
        Log.d(TAG, "safe")
        try {
            val country = currentCountry.value?.replace("Current Country: ", "") ?: "us"
            repository.getTopHeadlines(country, page).collectAndExecute(
                _liveData = _topHeadlinesArticles,
                getApplication()
            ) {
                Log.d(TAG, "safeTopHeadlinesCall: ${it.data.toString()}")
                _notificationArticle.send(it.data.articles[Random(10).nextInt()])
            }
        }catch (e: Exception) {
            Timber.d("Error $e")
        }
    }

    fun getCacheArticleContent(url: String) = viewModelScope.launch {
        _cacheArticleContent.value = Resources.Loading()
        try {
            val content = repository.getCacheArticleContent(url)
            _cacheArticleContent.value = Resources.Success(content)
        } catch (e: Exception) {
            _cacheArticleContent.value = Resources.Error(message = e.toString())
        }
    }

    fun saveCacheArticle(article: EntityArticle) = viewModelScope.launch {
        _cacheOperation.send(NewsCacheOperations.Loading)
        try {
            repository.saveArticle(article)
            _cacheOperation.send(NewsCacheOperations.Success("Saved Successfully"))
        } catch (e: Exception) {
            _cacheOperation.send(NewsCacheOperations.Error(message = e.toString()))
        }
    }

    fun getAllCacheArticles() = viewModelScope.launch {
        repository.getAllCacheArticles().collectAndExecute(
            _liveData = _cacheArticles,
            getApplication()
        ) {

        }
    }

    fun deleteCacheArticle(article: EntityArticle) = viewModelScope.launch {
        _cacheOperation.send(NewsCacheOperations.Loading)
        try {
            repository.deleteCacheArticle(article)
            _cacheOperation.send(NewsCacheOperations.Success("Deleted Successfully"))
        } catch (e: Exception) {
            _cacheOperation.send(NewsCacheOperations.Error(message = e.toString()))
        }
    }

    fun getArticleSources() = viewModelScope.launch {
        repository.getSources().collectAndExecute(
            _liveData = _sources,
            getApplication(),
        ){

        }
    }

}