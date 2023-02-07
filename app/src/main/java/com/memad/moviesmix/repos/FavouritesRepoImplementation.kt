package com.memad.moviesmix.repos

import com.memad.moviesmix.data.local.FavouritesEntity
import com.memad.moviesmix.data.local.MoviesDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class FavouritesRepoImplementation @Inject constructor(
    private val moviesDao: MoviesDao
) : FavouritesRepo {

    override suspend fun favouriteAMovie(movie: FavouritesEntity): Long {
        return moviesDao.insertFavouriteMovie(movie)
    }

    override suspend fun unFavouriteAMovie(movieId: Int) {
        moviesDao.deleteFavouriteMovie(movieId)
    }

    override suspend fun checkIsFavourite(movieId: Int): Boolean {
        return moviesDao.checkIsFavourite(movieId)
    }

    override suspend fun getFavouritesMovies(): Flow<List<FavouritesEntity>> {
        return moviesDao.getAllFavouriteMovies()
    }
}
