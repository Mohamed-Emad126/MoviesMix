package com.memad.moviesmix.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [MovieEntity::class, FavouritesEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converter::class)
abstract class MoviesDB : RoomDatabase() {
    abstract fun getYourDao(): MoviesDao
}