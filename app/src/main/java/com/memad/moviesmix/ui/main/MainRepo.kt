package com.memad.moviesmix.ui.main

import com.memad.moviesmix.data.local.MovieEntity
import com.memad.moviesmix.models.MoviesResponse
import com.memad.moviesmix.utils.Resource
import kotlinx.coroutines.flow.Flow

interface MainRepo {

    suspend fun favouriteAMovie(movie: MovieEntity)

    suspend fun unFavouriteAMovie(movieId: Int)

    suspend fun getAllMovies(
        page: Int
    ): Flow<Resource<out List<MovieEntity>>>
}
