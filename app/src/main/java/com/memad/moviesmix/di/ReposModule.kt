package com.memad.moviesmix.di

import com.memad.moviesmix.data.local.MoviesDao
import com.memad.moviesmix.data.remote.MoviesClient
import com.memad.moviesmix.di.annotations.*
import com.memad.moviesmix.repos.FavouritesRepoImplementation
import com.memad.moviesmix.repos.MainRepo
import com.memad.moviesmix.repos.MainRepoImpl
import com.memad.moviesmix.utils.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ReposModule {

    @PopularRepo
    @Singleton
    @Provides
    fun providePopularRepo(
        moviesDao: MoviesDao,
        moviesClient: MoviesClient
    ): MainRepo {
        return MainRepoImpl(moviesDao, moviesClient, Constants.POPULAR)
    }

    @TrendingRepo
    @Singleton
    @Provides
    fun provideTrendingRepo(
        moviesDao: MoviesDao,
        moviesClient: MoviesClient
    ): MainRepo {
        return MainRepoImpl(moviesDao, moviesClient, Constants.TRENDING)
    }

    @UpcomingRepo
    @Singleton
    @Provides
    fun provideUpcomingRepo(
        moviesDao: MoviesDao,
        moviesClient: MoviesClient
    ): MainRepo {
        return MainRepoImpl(moviesDao, moviesClient, Constants.UPCOMING)
    }
}