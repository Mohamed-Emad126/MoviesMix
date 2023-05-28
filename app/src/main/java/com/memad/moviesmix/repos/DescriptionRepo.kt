package com.memad.moviesmix.repos

import com.memad.moviesmix.data.local.MoviesDao
import com.memad.moviesmix.data.remote.MoviesClient
import com.memad.moviesmix.models.CastResponse
import com.memad.moviesmix.models.Movie
import com.memad.moviesmix.models.MoviesResponse
import com.memad.moviesmix.utils.AccessNative
import com.memad.moviesmix.utils.Resource
import com.skydoves.sandwich.getOrNull
import com.skydoves.sandwich.message
import com.skydoves.sandwich.suspendOnError
import com.skydoves.sandwich.suspendOnFailure
import com.skydoves.sandwich.suspendOnSuccess
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class DescriptionRepo @Inject constructor(
    private val moviesClient: MoviesClient,
    moviesDao: MoviesDao
) : FavouritesRepoImplementation(moviesDao) {

    suspend fun getMovie(movieId: String) = flow<Movie> {
        moviesClient.getMovieDetails(movieId, AccessNative.getApiKey())
    }

    suspend fun getSimilarMovies(movieId: String) = flow<Resource<MoviesResponse?>> {
        val response = moviesClient.getSimilarMovies(movieId, AccessNative.getApiKey())
        response.suspendOnSuccess {
            emit(Resource.Success(response.getOrNull()))
        }
            .suspendOnError {
                emit(Resource.Error(message(), null))
            }
            .suspendOnFailure {
                emit(Resource.Error(message(), null))
            }
    }

    suspend fun getCasts(movieId: String) = flow<Resource<CastResponse?>> {
        val response = moviesClient.getCastOfMovie(movieId, AccessNative.getApiKey())
        response.suspendOnSuccess {
            emit(Resource.Success(response.getOrNull()))
        }
            .suspendOnError {
                emit(Resource.Error(message(), null))
            }
            .suspendOnFailure {
                emit(Resource.Error(message(), null))
            }
    }
}