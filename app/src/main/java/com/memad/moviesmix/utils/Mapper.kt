package com.memad.moviesmix.utils

import com.memad.moviesmix.data.local.MovieEntity
import com.memad.moviesmix.models.MoviesResponse
import com.skydoves.sandwich.ApiResponse
import com.skydoves.sandwich.ApiSuccessModelMapper
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SuccessMoviesMapper @Inject constructor(private val movieType: Int) :
    ApiSuccessModelMapper<MoviesResponse, List<MovieEntity>> {

    override fun map(apiErrorResponse: ApiResponse.Success<MoviesResponse>): List<MovieEntity> {
        return apiErrorResponse.data.results.map {
            MovieEntity(movieType, apiErrorResponse.data.page,it)
        }
    }
}

