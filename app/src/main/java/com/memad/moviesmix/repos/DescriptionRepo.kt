package com.memad.moviesmix.repos

import com.memad.moviesmix.data.local.FavouritesEntity
import com.memad.moviesmix.data.local.MoviesDao
import com.memad.moviesmix.data.remote.MoviesClient
import com.memad.moviesmix.models.Movie
import com.memad.moviesmix.utils.AccessNative
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class DescriptionRepo @Inject constructor(
    private val moviesClient: MoviesClient,
    moviesDao: MoviesDao
) : FavouritesRepoImplementation(moviesDao) {

    suspend fun getMovie(movieId: String) = flow<Movie> {
        moviesClient.getMovieDetails(movieId, AccessNative.getApiKey())
    }
}