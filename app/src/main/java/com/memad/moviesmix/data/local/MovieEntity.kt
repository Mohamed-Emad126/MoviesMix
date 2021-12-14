package com.memad.moviesmix.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.memad.moviesmix.models.MoviesResponse


@Entity(tableName = "MOVIE_TABLE")
data class MovieEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "movie_id")
    val movieId: Int?,
    @ColumnInfo(name = "movie_type") val movieType: Int,
    @ColumnInfo(name = "movie_page") val moviePage: Int,
    @ColumnInfo(name = "movie") val movie: MoviesResponse.Result
) {

    @Ignore
    constructor(type: Int, page: Int, movie: MoviesResponse.Result) : this(null, type, page, movie)
}