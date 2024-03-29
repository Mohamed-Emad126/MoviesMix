package com.memad.moviesmix.data.remote

import com.memad.moviesmix.models.CastResponse
import com.memad.moviesmix.models.Movie
import com.memad.moviesmix.models.MoviesResponse
import com.memad.moviesmix.models.VideosResponse
import com.skydoves.sandwich.ApiResponse
import retrofit2.http.*

interface MoviesClient {

    @GET("3/movie/{movie_id}")
    suspend fun getMovieDetails(
        @Path("movie_id") movieId: String?,
        @Query("api_key") apiKey: String?,
        @Query("language") language: String = "en-US"
    ): ApiResponse<Movie>

    @GET("/3/trending/movie/day")
    suspend fun getTrendingMovies(
        @Query("api_key") apiKey: String?,
        @Query("page") page: Int,
        @Query("language") language: String = "en-US"
    ): ApiResponse<MoviesResponse>

    @GET("/3/movie/popular")
    suspend fun getPopularMovies(
        @Query("api_key") apiKey: String?,
        @Query("page") page: Int,
        @Query("language") language: String = "en-US"
    ): ApiResponse<MoviesResponse>

    @GET("/3/movie/upcoming")
    suspend fun getUpcomingMovies(
        @Query("api_key") apiKey: String?,
        @Query("page") page: Int,
        @Query("language") language: String = "en-US"
    ): ApiResponse<MoviesResponse>

    @GET("/3/search/movie")
    suspend fun searchInMovies(
        @Query("api_key") apiKey: String?,
        @Query("query") query: String?,
        @Query("page") page: Int,
        @Query("include_adult") includeAdult: Boolean = false,
        @Query("language") language: String = "en-US"
    ): ApiResponse<MoviesResponse>


    @GET("/3/movie/{movieId}/similar?page=1")
    suspend fun getSimilarMovies(
        @Path("movieId") movieId: String?,
        @Query("api_key") apiKey: String?,
        @Query("language") language: String = "en-US"
    ): ApiResponse<MoviesResponse>

    @GET("/3/movie/{movieId}/credits?")
    suspend fun getCastOfMovie(
        @Path("movieId") movieId: String?,
        @Query("api_key") apiKey: String?,
        @Query("language") language: String = "en-US"
    ): ApiResponse<CastResponse>

    @GET("/3/movie/{movieId}/videos?")
    suspend fun getVideosOfMovie(
        @Path("movieId") movieId: String?,
        @Query("api_key") apiKey: String?,
        @Query("language") language: String = "en-US"
    ): ApiResponse<VideosResponse>
}