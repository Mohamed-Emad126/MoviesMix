package com.memad.moviesmix.ui.main.description

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.memad.moviesmix.data.local.FavouritesEntity
import com.memad.moviesmix.data.local.MovieEntity
import com.memad.moviesmix.models.Movie
import com.memad.moviesmix.repos.DescriptionRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MovieDescriptionViewModel @Inject constructor(
    private val descriptionRepo: DescriptionRepo
) :
    ViewModel() {

    private val _movie = MutableSharedFlow<Movie>()
    val movie = _movie.asSharedFlow()


    private val _isFavourite = MutableStateFlow(false)
    val isFavourite = _isFavourite.asStateFlow()


    fun getMovie(movieId: Int) {
        viewModelScope.launch {
            descriptionRepo.getMovie(movieId.toString()).collectLatest {
                _movie.emit(it)
            }
        }
    }

    fun addToFavourites(entity: MovieEntity) {
        viewModelScope.launch {
            descriptionRepo.favouriteAMovie(
                FavouritesEntity(entity.movieId, entity.movie)
            )
        }
        checkIsFavourites(entity.movieId!!)
    }

    fun removeFromFavourites(movieId: Int) {
        viewModelScope.launch {
            descriptionRepo.unFavouriteAMovie(movieId)
        }
        checkIsFavourites(movieId)
    }

    fun checkIsFavourites(movieId: Int) {
        viewModelScope.launch {
            _isFavourite.value = (descriptionRepo.checkIsFavourite(movieId))
            _isFavourite.emit(descriptionRepo.checkIsFavourite(movieId))
        }
    }

}