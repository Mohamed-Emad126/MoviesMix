package com.memad.moviesmix.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MoviesDao {
    @Insert(onConflict = REPLACE)
    suspend fun insertMovies(movies: List<MovieEntity>): Array<Long>

    @Insert(onConflict = REPLACE)
    suspend fun insertFavouriteMovie(movies: MovieEntity): Long

    @Query("DELETE FROM MOVIE_TABLE WHERE movie_type = :type AND movie_page =:page")
    suspend fun deleteMovies(type: Int, page: Int): Int

    @Query("DELETE FROM MOVIE_TABLE WHERE movie_type = 3 AND Movie_id = :movieId")
    suspend fun deleteFavouriteMovie(movieId: Int): Int

    @Query("SELECT EXISTS(SELECT 1 FROM MOVIE_TABLE WHERE movie_type= 3 AND movie LIKE '%' || '\"id\": ' || :apiMovieId || '%' )")
    suspend fun checkIsFavourite(apiMovieId: Int): Int

    @Query("SELECT * FROM MOVIE_TABLE WHERE movie_type = :type AND movie_page =:page")
    fun getAllMovies(type: Int, page: Int): Flow<List<MovieEntity>>

    @Query("SELECT * FROM MOVIE_TABLE WHERE movie_type = :type AND movie_page IN(:page)")
    fun getWholeDB(type: Int, page: List<Int>): Flow<List<MovieEntity>>

}