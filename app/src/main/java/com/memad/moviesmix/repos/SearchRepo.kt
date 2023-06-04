package com.memad.moviesmix.repos

import com.memad.moviesmix.data.local.MoviesDao
import com.memad.moviesmix.data.remote.MoviesClient
import com.memad.moviesmix.models.MoviesResponse
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

class SearchRepo @Inject constructor(
    moviesDao: MoviesDao,
    private val moviesClient: MoviesClient,
    private val preferencesHelper: SharedPreferencesHelper
) : FavouritesRepoImplementation(moviesDao) {

    suspend fun searchMovies(
        query: String,
        page: Int
    ) = flow<Resource<MoviesResponse?>> {
        val response = moviesClient.searchInMovies(
            apiKey = AccessNative.getApiKey(),
            query = query,
            page = page,
            language = if(preferencesHelper.read(Constants.LANG_PREF, "en-US") == "0") "ar-EG" else "en-US"
        )
        emit(Resource.Loading())
        response.suspendOnSuccess {
            emit(Resource.Success(response.getOrNull()))
        }
            .suspendOnError {
                emit(Resource.Error(errorMessage = message()))
            }
            .suspendOnFailure {
                emit(Resource.Error(errorMessage = message()))
            }
    }
}