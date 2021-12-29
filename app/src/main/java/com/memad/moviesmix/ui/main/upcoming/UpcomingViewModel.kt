package com.memad.moviesmix.ui.main.upcoming

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.memad.moviesmix.data.local.MovieEntity
import com.memad.moviesmix.di.annotations.TrendingRepo
import com.memad.moviesmix.di.annotations.UpcomingRepo
import com.memad.moviesmix.repos.MainRepo
import com.memad.moviesmix.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UpcomingViewModel @Inject constructor(@UpcomingRepo private val mainRepoImpl: MainRepo) :
    ViewModel() {

    private val currentPage = MutableLiveData(1)
    val moviesListLiveData = MutableLiveData<MutableList<MovieEntity>>(mutableListOf())
    private val _moviesResource =
        MutableSharedFlow<Resource<out List<MovieEntity>>>()
    val moviesResource: SharedFlow<Resource<out List<MovieEntity>>> = _moviesResource
    private val _moviesList = MutableSharedFlow<MutableList<MovieEntity>>()
    val moviesList = _moviesList.asSharedFlow()

    init {
        getMovies(currentPage.value!!)
    }

    private fun getMovies(page: Int) {
        viewModelScope.launch {
            mainRepoImpl.getAllMovies(
                page
            ).collect {
                if (currentPage.value == 1) {
                    moviesListLiveData.value?.clear()
                    moviesListLiveData.value = moviesListLiveData.value
                }
                if (!it.data.isNullOrEmpty()) {
                    moviesListLiveData.value?.addAll(it.data)
                    _moviesList.emit(moviesListLiveData.value!!)
                }
                _moviesResource.emit(it)
            }
        }
    }

    fun loadNextPage() {
        currentPage.value =
            moviesListLiveData.value?.get(moviesListLiveData.value?.size!! - 2)?.moviePage!! + 1
        getMovies(currentPage.value!!)
    }

    fun reload() {
        currentPage.value = moviesListLiveData.value?.last()?.moviePage!!
        getMovies(currentPage.value!!)
    }
}