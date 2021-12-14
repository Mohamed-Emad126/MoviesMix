package com.memad.moviesmix.ui.main.popular

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.memad.moviesmix.data.local.MovieEntity
import com.memad.moviesmix.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PopularViewModel @Inject constructor(private val popularRepo: PopularRepo) : ViewModel() {

    private val currentPage = MutableLiveData(1)
    private val _isFirstLoading = MutableSharedFlow<Boolean>()
    val moviesListLiveData = MutableLiveData<MutableList<MovieEntity>>(mutableListOf())
    val isFirstLoading: SharedFlow<Boolean> = _isFirstLoading
    private val _moviesResource =
        MutableSharedFlow<Resource<out List<MovieEntity>>>()
    val moviesResource: SharedFlow<Resource<out List<MovieEntity>>> = _moviesResource
    private val _moviesList = MutableSharedFlow<MutableList<MovieEntity>>()
    val moviesList = _moviesList.asSharedFlow()

    init {
        viewModelScope.launch {
            _isFirstLoading.emit(true)
        }
        getMovies(currentPage.value!!)
    }

    private fun getMovies(page: Int) {
        viewModelScope.launch {
            popularRepo.getAllMovies(
                page
            ).collect {
                if (currentPage.value == 1) {
                    moviesListLiveData.value?.clear()
                    moviesListLiveData.value = moviesListLiveData.value
                }
                _isFirstLoading.emit(false)
                Log.i("TAG: pop VIM:", "before_moviesList :-> ${moviesListLiveData.value?.size}")
                if (!it.data.isNullOrEmpty()) {
                    Log.i("TAG: pop VIM:", "isNullOrEmpty :-> ${it.data.size}")
                    moviesListLiveData.value?.addAll(it.data)
                    _moviesList.emit(moviesListLiveData.value!!)
                    Log.i("TAG: pop VIM:", "after_moviesList :-> ${moviesListLiveData.value?.size}")
                }
                _moviesResource.emit(it)
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
}