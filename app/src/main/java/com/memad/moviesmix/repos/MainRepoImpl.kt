package com.memad.moviesmix.repos

import com.memad.moviesmix.data.local.MovieEntity
import com.memad.moviesmix.data.local.MoviesDao
import com.memad.moviesmix.data.remote.MoviesClient
import com.memad.moviesmix.utils.AccessNative
import com.memad.moviesmix.utils.Constants
import com.memad.moviesmix.utils.Resource
import com.memad.moviesmix.utils.SuccessMoviesMapper
import com.skydoves.sandwich.message
import com.skydoves.sandwich.suspendOnError
import com.skydoves.sandwich.suspendOnFailure
import com.skydoves.sandwich.suspendOnSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class MainRepoImpl @Inject constructor(
    private val moviesDao: MoviesDao,
    private val moviesClient: MoviesClient,
    private val type: Int
) : MainRepo {

    override suspend fun favouriteAMovie(movie: MovieEntity) {
        moviesDao.insertFavouriteMovie(movie)
    }

    override suspend fun unFavouriteAMovie(movieId: Int) {
        moviesDao.deleteFavouriteMovie(movieId)
    }

    override suspend fun getAllMovies(
        page: Int
    ) = flow {
        val response = when(type){
            Constants.POPULAR -> moviesClient.getPopularMovies(AccessNative.getApiKey(), page)
            Constants.UPCOMING -> moviesClient.getTrendingMovies(AccessNative.getApiKey(), page)
            Constants.TRENDING -> moviesClient.getUpcomingMovies(AccessNative.getApiKey(), page)
            else -> moviesClient.getPopularMovies(AccessNative.getApiKey(), page)
        }
        val cachedMovies = moviesDao.getAllMovies(type, page).first()

        response.suspendOnSuccess(SuccessMoviesMapper(type)) {
            moviesDao.deleteMovies(type, page)
            moviesDao.insertMovies(this)
            val allMovies = moviesDao.getAllMovies(type, page).first()
            emit(Resource.Success(allMovies))
        }.suspendOnError {
            emit(Resource.Error(message(), cachedMovies))
        }.suspendOnFailure {
            emit(Resource.Error(message(), cachedMovies))
        }
    }.flowOn(Dispatchers.IO)

}