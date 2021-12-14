package com.memad.moviesmix.ui.main.popular

import android.util.Log
import com.memad.moviesmix.data.local.MovieEntity
import com.memad.moviesmix.data.local.MoviesDao
import com.memad.moviesmix.data.remote.MoviesClient
import com.memad.moviesmix.ui.main.MainRepo
import com.memad.moviesmix.utils.AccessNative
import com.memad.moviesmix.utils.Constants
import com.memad.moviesmix.utils.Resource
import com.memad.moviesmix.utils.SuccessMoviesMapper
import com.skydoves.sandwich.message
import com.skydoves.sandwich.suspendOnError
import com.skydoves.sandwich.suspendOnFailure
import com.skydoves.sandwich.suspendOnSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class PopularRepo @Inject constructor(
    private val moviesDao: MoviesDao,
    private val moviesClient: MoviesClient,
) : MainRepo {
    private val type: Int = Constants.POPULAR

    override suspend fun favouriteAMovie(movie: MovieEntity) {
        moviesDao.insertFavouriteMovie(movie)
    }

    override suspend fun unFavouriteAMovie(movieId: Int) {
        moviesDao.deleteFavouriteMovie(movieId)
    }

    override suspend fun getAllMovies(
        page: Int
    ) = flow {
        val response = moviesClient.getPopularMovies(AccessNative.getApiKey(), page)
        val cachedMovies = moviesDao.getAllMovies(type, page).first()
        response.suspendOnSuccess(SuccessMoviesMapper(type)) {
            moviesDao.deleteMovies(type, page)
            moviesDao.insertMovies(this)
            val allMovies = moviesDao.getAllMovies(type, page).first()
            Log.i("TAG: pop repo:", "${this.size} :-> ${allMovies.size}")
            emit(Resource.Success(allMovies))
        }.suspendOnError {
            Log.i("TAG: pop repo:", "onError :-> ${cachedMovies.size}")
            emit(Resource.Error(message(), cachedMovies))
        }.suspendOnFailure {
            Log.i("TAG: pop repo:", "onFailure :-> ${cachedMovies.size}")
            emit(Resource.Error(this, cachedMovies))
        }
    }.flowOn(Dispatchers.IO)

}