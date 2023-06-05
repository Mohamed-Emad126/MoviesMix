package com.memad.moviesmix.ui.main.search

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.memad.moviesmix.data.local.FavouritesEntity
import com.memad.moviesmix.data.local.MovieEntity
import com.memad.moviesmix.models.MoviesResponse
import com.memad.moviesmix.repos.SearchRepo
import com.memad.moviesmix.utils.NetworkStatus
import com.memad.moviesmix.utils.NetworkStatusHelper
import com.memad.moviesmix.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchRepo: SearchRepo,
    networkStatusHelper: NetworkStatusHelper
) : ViewModel() {
    private val _searchStatus = MutableStateFlow<Resource<out MoviesResponse?>>(Resource.Loading())
    val searchStatus = _searchStatus.asStateFlow()

    val networkStatus: StateFlow<NetworkStatus> = networkStatusHelper.networkStatus.stateIn(
        initialValue = NetworkStatus.Unknown,
        scope = viewModelScope,
        started = WhileSubscribed(5000)
    )
    private val searchResult =
        MutableSharedFlow<MutableList<MoviesResponse.Result>>(replay = 1)
    val searchResultFlow = searchResult.asSharedFlow()

    private val checkFavourites =
        MutableSharedFlow<MutableList<Boolean>>(replay = 1)
    val checkFavouritesFlow = checkFavourites.asSharedFlow()


    private val currentPage = MutableLiveData(1)

    fun searchMovies(query: String) {
        viewModelScope.launch {
            _searchStatus.emit(Resource.Loading())
            searchRepo.searchMovies(query, 1).collect {
                _searchStatus.emit(it)
                if (it is Resource.Success) {
                    searchResult.emit(it.data?.results!!.toMutableList())
                    checkIsFavourites(it.data.results.map { i -> i.id })
                }
            }
        }
    }

    fun loadMore(query: String) {
        val page = currentPage.value?.plus(1)
        currentPage.value = page
        viewModelScope.launch {
            searchRepo.searchMovies(query, page!!).collect {
                _searchStatus.emit(it)
                if (it is Resource.Success) {
                    searchResult.emit(
                        listOf(
                            searchResult.first(),
                            it.data?.results!!.toMutableList()
                        ).flatten().toMutableList()
                    )
                    checkIsFavourites(searchResult.first().map { i -> i.id })
                }
                checkIsFavourites(searchResult.first().map { i -> i.id })
            }
        }
    }

    fun removeFromFavourites(movieId: Int) {
        viewModelScope.launch {
            searchRepo.unFavouriteAMovie(movieId)
            checkIsFavourites(searchResult.first().map { i -> i.id })
        }
    }

    private fun checkIsFavourites(ids: List<Int>) {
        viewModelScope.launch {
            val list = mutableListOf<Boolean>()
            ids.forEach {
                list.add(searchRepo.checkIsFavourite(it))
            }
            checkFavourites.emit(list)
        }
    }

    fun addToFavourites(entity: MovieEntity) {
        viewModelScope.launch {
            searchRepo.favouriteAMovie(
                FavouritesEntity(entity.movie?.id, entity.movie)
            )
            checkIsFavourites(searchResult.first().map { i -> i.id })
        }
    }
}