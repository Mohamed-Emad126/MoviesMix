package com.memad.moviesmix.di

import com.memad.moviesmix.data.local.MoviesDao
import com.memad.moviesmix.data.remote.MoviesClient
import com.memad.moviesmix.di.annotations.*
import com.memad.moviesmix.repos.FavouritesRepo
import com.memad.moviesmix.repos.FavouritesRepoImplementation
import com.memad.moviesmix.repos.MainRepo
import com.memad.moviesmix.repos.MainRepoImpl
import com.memad.moviesmix.utils.Constants
import com.memad.moviesmix.utils.SharedPreferencesHelper
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
        moviesClient: MoviesClient,
        preferencesHelper: SharedPreferencesHelper
    ): MainRepo {
        return MainRepoImpl(moviesDao, moviesClient, Constants.POPULAR, preferencesHelper)
    }

    @TrendingRepo
    @Singleton
    @Provides
    fun provideTrendingRepo(
        moviesDao: MoviesDao,
        moviesClient: MoviesClient,
        preferencesHelper: SharedPreferencesHelper
    ): MainRepo {
        return MainRepoImpl(moviesDao, moviesClient, Constants.TRENDING, preferencesHelper)
    }

    @UpcomingRepo
    @Singleton
    @Provides
    fun provideUpcomingRepo(
        moviesDao: MoviesDao,
        moviesClient: MoviesClient,
        preferencesHelper: SharedPreferencesHelper
    ): MainRepo {
        return MainRepoImpl(moviesDao, moviesClient, Constants.UPCOMING, preferencesHelper)
    }

    @FavouritesRepoAnn
    @Singleton
    @Provides
    fun provideFavouritesRepo(
        moviesDao: MoviesDao
    ): FavouritesRepo {
        return FavouritesRepoImplementation(moviesDao)
    }
}