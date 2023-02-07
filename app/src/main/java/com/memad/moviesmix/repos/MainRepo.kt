package com.memad.moviesmix.repos

import com.memad.moviesmix.data.local.FavouritesEntity
import com.memad.moviesmix.data.local.MovieEntity
import com.memad.moviesmix.models.MoviesResponse
import com.memad.moviesmix.utils.Resource
import kotlinx.coroutines.flow.Flow

interface MainRepo {

    suspend fun favouriteAMovie(movie: FavouritesEntity): Long

    suspend fun unFavouriteAMovie(movieId: Int)

    suspend fun getAllMovies(
        page: Int
    ): Flow<Resource<out List<MovieEntity>>>
}
