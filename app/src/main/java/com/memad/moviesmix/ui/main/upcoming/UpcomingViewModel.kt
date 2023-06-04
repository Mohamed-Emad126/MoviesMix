package com.memad.moviesmix.ui.main.upcoming

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.memad.moviesmix.data.local.FavouritesEntity
import com.memad.moviesmix.data.local.MovieEntity
import com.memad.moviesmix.di.annotations.UpcomingRepo
import com.memad.moviesmix.repos.MainRepo
import com.memad.moviesmix.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UpcomingViewModel @Inject constructor(@UpcomingRepo private val mainRepoImpl: MainRepo) :
    ViewModel() {
    private val currentPage = MutableLiveData(1)
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
                if (it is Resource.Success) {
                    it.data?.let { it1 ->
                        checkIsFavourites(it1.map { i -> i.movie?.id!! })
                        _moviesList.emit(it1.toMutableList())
                    }
                }
            }
        }
    }

    fun loadNextPage() {
        val page = currentPage.value?.plus(1)
        currentPage.value = page
        viewModelScope.launch {
            mainRepoImpl.getAllMovies(page!!).collect {
                _moviesResource.emit(it)
                if (it is Resource.Success) {
                    it.data?.map { i -> i.movie?.id!! }.let { it1 -> checkIsFavourites(it1!!) }
                    _moviesList.emit(
                        listOf(
                            _moviesList.first(),
                            it.data!!.toMutableList()
                        ).flatten().toMutableList()
                    )
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
            checkIsFavourites(_moviesList.first().filter { it.moviePage != -122 }
                .map { i -> i.movieId!! })
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