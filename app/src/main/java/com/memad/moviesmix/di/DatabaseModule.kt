package com.memad.moviesmix.di

import android.content.Context
import androidx.room.Room
import com.memad.moviesmix.data.local.MoviesDB
import com.memad.moviesmix.data.local.MoviesDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {

    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext context: Context) = Room.databaseBuilder(
        context,
        MoviesDB::class.java,
        "MOVIES_BD"
    ).build()

    @Singleton
    @Provides
    fun provideMoviesDao(moviesDB: MoviesDB): MoviesDao {
        return moviesDB.getYourDao()
    }

}