package com.memad.moviesmix.repos

import com.memad.moviesmix.data.local.MoviesDao
import com.memad.moviesmix.data.remote.MoviesClient
import com.memad.moviesmix.models.CastResponse
import com.memad.moviesmix.models.Movie
import com.memad.moviesmix.models.MoviesResponse
import com.memad.moviesmix.models.VideosResponse
import com.memad.moviesmix.utils.AccessNative
import com.memad.moviesmix.utils.Constants
import com.memad.moviesmix.utils.Resource
import com.memad.moviesmix.utils.SharedPreferencesHelper
import com.skydoves.sandwich.getOrNull
import com.skydoves.sandwich.message
import com.skydoves.sandwich.suspendOnError
import com.skydoves.sandwich.suspendOnFailure
import com.skydoves.sandwich.suspendOnSuccess
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class DescriptionRepo @Inject constructor(
    private val moviesClient: MoviesClient,
    moviesDao: MoviesDao,
    private val preferencesHelper: SharedPreferencesHelper
) : FavouritesRepoImplementation(moviesDao) {

    suspend fun getMovie(movieId: String) = flow<Movie> {
        moviesClient.getMovieDetails(movieId, AccessNative.getApiKey())
    }

    suspend fun getSimilarMovies(movieId: String) = flow<Resource<MoviesResponse?>> {
        val response = moviesClient.getSimilarMovies(
            movieId, AccessNative.getApiKey(),
            language = if(preferencesHelper.read(Constants.LANG_PREF, "en-US") == "0") "ar-EG" else "en-US"
        )
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
        val response = moviesClient.getCastOfMovie(
            movieId,
            AccessNative.getApiKey(),
            language = if(preferencesHelper.read(Constants.LANG_PREF, "en-US") == "0") "ar-EG" else "en-US"
        )
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

    fun getVideos(toString: String) = flow<Resource<VideosResponse?>> {
        val response = moviesClient.getVideosOfMovie(toString, AccessNative.getApiKey(),
            language = if(preferencesHelper.read(Constants.LANG_PREF, "en-US") == "0") "ar-EG" else "en-US")
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