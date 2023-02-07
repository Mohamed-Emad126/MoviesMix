package com.memad.moviesmix.ui.main.popular

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.memad.moviesmix.data.local.MovieEntity
import com.memad.moviesmix.di.annotations.PopularRepo
import com.memad.moviesmix.repos.MainRepo
import com.memad.moviesmix.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

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
                //Log.i("TAG: pop VIM:", "before_moviesList :-> ${moviesListLiveData.value?.size}")
                if (!it.data.isNullOrEmpty()) {
                    //Log.i("TAG: pop VIM:", "isNullOrEmpty :-> ${it.data.size}")
                    moviesListLiveData.value?.addAll(it.data)
                    _moviesList.emit(moviesListLiveData.value?.toMutableSet()!!)
                    //Log.i("TAG: pop VIM:", "after_moviesList :-> ${moviesListLiveData.value?.size}")
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
}