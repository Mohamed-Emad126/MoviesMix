package com.memad.moviesmix.repos

import com.memad.moviesmix.data.local.FavouritesEntity
import kotlinx.coroutines.flow.Flow

interface FavouritesRepo {

    suspend fun favouriteAMovie(movie: FavouritesEntity): Long

    suspend fun unFavouriteAMovie(movieId: Int): Flow<Int>

    suspend fun checkIsFavourite(movieId: Int): Boolean

    suspend fun getFavouritesMovies(): Flow<List<FavouritesEntity>>
}
