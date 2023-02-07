package com.memad.moviesmix.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.memad.moviesmix.models.MoviesResponse


@Entity(tableName = "FAVOURITES_TABLE")
data class FavouritesEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "movie_id")
    val movieId: Int?,
    @ColumnInfo(name = "movie") val movie: MoviesResponse.Result?
)