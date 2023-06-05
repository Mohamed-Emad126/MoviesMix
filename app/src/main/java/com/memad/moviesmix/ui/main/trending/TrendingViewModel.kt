package com.memad.moviesmix.ui.main.trending

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.memad.moviesmix.data.local.FavouritesEntity
import com.memad.moviesmix.data.local.MovieEntity
import com.memad.moviesmix.di.annotations.TrendingRepo
import com.memad.moviesmix.repos.MainRepo
import com.memad.moviesmix.utils.NetworkStatus
import com.memad.moviesmix.utils.NetworkStatusHelper
import com.memad.moviesmix.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrendingViewModel @Inject constructor(
    @TrendingRepo private val mainRepoImpl: MainRepo,
    networkStatusHelper: NetworkStatusHelper
) :
    ViewModel() {
    private val currentPage = MutableLiveData(1)


    val networkStatus: StateFlow<NetworkStatus> = networkStatusHelper.networkStatus.stateIn(
        initialValue = NetworkStatus.Unknown,
        scope = viewModelScope,
        started = WhileSubscribed(5000)
    )

    private val _moviesResource =
        MutableSharedFlow<Resource<out List<MovieEntity>>>(replay = 1)
    val moviesResource = _moviesResource.asSharedFlow()

    private val _moviesList = MutableSharedFlow<MutableList<MovieEntity>>(replay = 1)
    val moviesList = _moviesList.asSharedFlow()

    private val checkFavourites =
        MutableSharedFlow<MutableList<Boolean>>(replay = 1)
    val checkFavouritesFlow = checkFavourites.asSharedFlow()

    init {
        getMovies(currentPage.value!!)
    }

    private fun getMovies(page: Int = currentPage.value!!) {
        viewModelScope.launch {
            mainRepoImpl.getAllMovies(
                page
            ).collect {
                _moviesResource.emit(it)
                if (it is Resource.Success || (it is Resource.Error && it.data != null)) {
                    it.data?.let { it1 ->
                        _moviesList.emit(it1.toMutableList())
                        _moviesList.first().filter { it2 -> it2.moviePage != -122 }
                            .map { i -> i.movieId!! }.let { it2 ->
                                checkIsFavourites(it2)
                            }
                    }
                }
            }
        }
    }

    fun loadNextPage() {
        val page = currentPage.value?.plus(1)
        currentPage.value = page
        viewModelScope.launch {
            mainRepoImpl.getAllMovies(page!!).collect { resource ->
                _moviesResource.emit(resource)
                if (resource is Resource.Success) {
                    _moviesList.emit(
                        listOf(
                            _moviesList.first(),
                            resource.data!!.toMutableList()
                        ).flatten().toMutableList()
                    )
                    _moviesList.first().filter { it.moviePage != -122 }
                        .map { i -> i.movieId!! }.let { it1 ->
                            checkIsFavourites(it1)
                        }
                }
            }
        }
    }

    fun removeFromFavourites(movieId: Int) {
        viewModelScope.launch {
            mainRepoImpl.unFavouriteAMovie(movieId).collectLatest { id ->
                if (id != -1) {
                    checkIsFavourites(_moviesList.first().filter { it.moviePage != -122 }
                        .map { i -> i.movieId!! })
                }
            }
        }
    }

    private fun checkIsFavourites(ids: List<Int>) {
        viewModelScope.launch {
            val list = mutableListOf<Boolean>()
            ids.forEach {
                list.add(mainRepoImpl.checkIsFavourite(it))
            }
            checkFavourites.emit(list)
        }
    }

    fun addToFavourites(entity: MovieEntity) {
        viewModelScope.launch {
            mainRepoImpl.favouriteAMovie(
                FavouritesEntity(entity.movie?.id, entity.movie)
            )
            checkIsFavourites(_moviesList.first().filter { it.moviePage != -122 }
                .map { i -> i.movieId!! })
        }
    }
}