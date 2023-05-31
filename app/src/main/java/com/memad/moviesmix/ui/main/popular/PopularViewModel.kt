package com.memad.moviesmix.ui.main.popular

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.memad.moviesmix.data.local.FavouritesEntity
import com.memad.moviesmix.data.local.MovieEntity
import com.memad.moviesmix.di.annotations.PopularRepo
import com.memad.moviesmix.repos.MainRepo
import com.memad.moviesmix.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PopularViewModel @Inject constructor(@PopularRepo private val mainRepoImpl: MainRepo) :
    ViewModel() {

    private val currentPage = MutableLiveData(1)
    val moviesListLiveData = MutableLiveData<MutableSet<MovieEntity>>(mutableSetOf())

    private val _isFirstLoading = MutableStateFlow(true)
    val isFirstLoading: StateFlow<Boolean> = _isFirstLoading.asStateFlow()

    private val _moviesResource =
        MutableStateFlow<Resource<out List<MovieEntity>>>(Resource.Loading())
    val moviesResource: StateFlow<Resource<out List<MovieEntity>>> = _moviesResource.asStateFlow()

    private val _moviesList = MutableSharedFlow<MutableSet<MovieEntity>>()
    val moviesList = _moviesList.asSharedFlow()

    private val checkFavourites =
        MutableSharedFlow<MutableList<Boolean>>(replay = 1)
    val checkFavouritesFlow = checkFavourites.asSharedFlow()

    init {
        getMovies(currentPage.value!!)
    }

    private fun getMovies(page: Int) {
        viewModelScope.launch {
            mainRepoImpl.getAllMovies(
                page
            ).collect {
                if (page == 1) {
                    moviesListLiveData.value?.clear()
                }
                _isFirstLoading.emit(false)
                if (!it.data.isNullOrEmpty()) {
                    moviesListLiveData.value?.addAll(it.data)
                    _moviesList.emit(moviesListLiveData.value?.toMutableSet()!!)
                    checkIsFavourites(moviesListLiveData.value?.map { i -> i.movie?.id!! }!!)
                }
                _moviesResource.value = it
            }
        }
    }

    fun loadNextPage() {
        currentPage.value = moviesListLiveData.value?.last()?.moviePage!! + 1
        getMovies(currentPage.value!!)
    }

    fun refresh() {
        currentPage.value = 1
        getMovies(currentPage.value!!)
    }

    fun setIsFirstLoading(value: Boolean) {
        viewModelScope.launch {
            _isFirstLoading.emit(value)
        }
    }

    fun checkIsFavourites(ids: List<Int> = moviesListLiveData.value?.map { i -> i.movie?.id!! }!!) {
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
            moviesListLiveData.value?.let { checkIsFavourites(it.map { i -> i.movie?.id!! }) }
        }
    }
}