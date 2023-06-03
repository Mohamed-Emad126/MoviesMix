package com.memad.moviesmix.ui.main.favourites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.memad.moviesmix.data.local.FavouritesEntity
import com.memad.moviesmix.di.annotations.FavouritesRepoAnn
import com.memad.moviesmix.repos.FavouritesRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavouritesViewModel @Inject constructor(
    @FavouritesRepoAnn private val favouritesRepo: FavouritesRepo
) : ViewModel() {

    private val favoriteMovies =
        MutableSharedFlow<MutableList<FavouritesEntity>>(replay = 1)
    val favoriteMoviesFlow = favoriteMovies.asSharedFlow()

    private val deleteStatus = MutableSharedFlow<Boolean>(replay = 1)
    val deleteStatusFlow = deleteStatus.asSharedFlow()

    init {
        getFavoriteMovies()
    }

    fun removeFromFavourites(movieId: Int) {
        viewModelScope.launch {
            deleteStatus.emit(false)
            favouritesRepo.unFavouriteAMovie(movieId).collect {
                deleteStatus.emit(it > 0)
            }
        }
    }

    fun getFavoriteMovies() {
        viewModelScope.launch {
            favouritesRepo.getFavouritesMovies().collect {
                favoriteMovies.emit(it.toMutableList())
            }
        }
    }
}