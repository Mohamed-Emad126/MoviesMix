package com.memad.moviesmix.ui.main.description

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.memad.moviesmix.data.local.FavouritesEntity
import com.memad.moviesmix.data.local.MovieEntity
import com.memad.moviesmix.models.CastResponse
import com.memad.moviesmix.models.Movie
import com.memad.moviesmix.models.MoviesResponse
import com.memad.moviesmix.models.VideosResponse
import com.memad.moviesmix.repos.DescriptionRepo
import com.memad.moviesmix.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DescriptionViewModel @Inject constructor(
    private val descriptionRepo: DescriptionRepo
) :
    ViewModel() {

    private val _movie = MutableSharedFlow<Movie>()
    val movie = _movie.asSharedFlow()

    private val _similarMovies = MutableStateFlow<Resource<out MoviesResponse?>>(Resource.Loading())
    val similarMovies = _similarMovies.asStateFlow()

    private val _casts = MutableStateFlow<Resource<out CastResponse?>>(Resource.Loading())
    val cast = _casts.asStateFlow()

    private val _videos = MutableStateFlow<Resource<out VideosResponse?>>(Resource.Loading())
    val videos = _videos.asStateFlow()


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
                FavouritesEntity(entity.movie?.id, entity.movie)
            )
        }
        checkIsFavourites(entity.movie?.id!!)
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
            _isFavourite.emit(
                descriptionRepo.checkIsFavourite(movieId)
            )
        }
    }

    fun getSimilarMovies(movieId: String) {
        viewModelScope.launch {
            descriptionRepo.getSimilarMovies(movieId).collect {
                _similarMovies.emit(it)
            }
        }
    }

    fun getCastOfMovie(movieId: String) {
        viewModelScope.launch {
            descriptionRepo.getCasts(movieId).collect {
                _casts.emit(it)
            }
        }
    }

    fun getVideo(movieId: String) {
        viewModelScope.launch {
            descriptionRepo.getVideos(movieId).collect {
                _videos.emit(it)
            }
        }
    }

}