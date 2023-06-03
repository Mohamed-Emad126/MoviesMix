package com.memad.moviesmix.repos

import com.memad.moviesmix.data.local.FavouritesEntity
import com.memad.moviesmix.data.local.MoviesDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class FavouritesRepoImplementation @Inject constructor(
    private val moviesDao: MoviesDao
) : FavouritesRepo {

    override suspend fun favouriteAMovie(movie: FavouritesEntity): Long {
        Mutex().withLock {
            return moviesDao.insertFavouriteMovie(movie)
        }
    }

    override suspend fun unFavouriteAMovie(movieId: Int) = flow<Int> {
        moviesDao.deleteFavouriteMovie(movieId)
    }


    override suspend fun checkIsFavourite(movieId: Int): Boolean {
        return moviesDao.checkIsFavourite(movieId)
    }

    override suspend fun getFavouritesMovies(): Flow<List<FavouritesEntity>> {
        Mutex().withLock {
            return moviesDao.getAllFavouriteMovies()
        }
    }
}

